package com.curlite.service;

import com.curlite.dto.ApiRequestDTO;
import com.curlite.dto.ApiResponseDTO;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.util.Timeout;

public class RequestExecutionService {

    private static final int MAX_BODY_SIZE = 1_048_576;
    private static final Set<String> ALLOWED_METHODS = new HashSet<>(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
    private final HistoryService historyService = new HistoryService();

    public ApiResponseDTO execute(ApiRequestDTO apiRequestDTO) {
        validate(apiRequestDTO);
        long start = System.currentTimeMillis();

        RequestConfig requestConfig = RequestConfig.custom()
            .setResponseTimeout(Timeout.ofSeconds(10))
            .setConnectionRequestTimeout(Timeout.ofSeconds(10))
            .build();

        ApiResponseDTO responseDTO = new ApiResponseDTO();

        try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {
            ClassicHttpRequest request = buildRequest(apiRequestDTO);
            try (ClassicHttpResponse response = client.executeOpen(null, request, null)) {
                responseDTO.setStatus(response.getCode());
                responseDTO.setHeaders(flattenHeaders(response));
                HttpEntity entity = response.getEntity();
                responseDTO.setBody(entity == null ? "" : EntityUtils.toString(entity, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            responseDTO.setStatus(500);
            responseDTO.setError("Execution failed: " + e.getMessage());
            responseDTO.setBody("");
        }

        long elapsed = System.currentTimeMillis() - start;
        responseDTO.setResponseTime(elapsed);
        historyService.saveExecution(
            apiRequestDTO.getMethod().toUpperCase(Locale.ROOT),
            apiRequestDTO.getUrl(),
            responseDTO.getStatus(),
            elapsed
        );

        return responseDTO;
    }

    private ClassicHttpRequest buildRequest(ApiRequestDTO dto) {
        String method = dto.getMethod().toUpperCase(Locale.ROOT);
        ClassicRequestBuilder builder = ClassicRequestBuilder.create(method).setUri(dto.getUrl());

        if (dto.getHeaders() != null) {
            dto.getHeaders().forEach((k, v) -> {
                if (k != null && v != null) {
                    builder.addHeader(k, v);
                }
            });
        }

        if (dto.getBody() != null && !dto.getBody().isBlank() && supportsBody(method)) {
            builder.setEntity(new StringEntity(dto.getBody(), StandardCharsets.UTF_8));
        }

        return builder.build();
    }

    private boolean supportsBody(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method);
    }

    private Map<String, String> flattenHeaders(ClassicHttpResponse response) {
        java.util.LinkedHashMap<String, String> headers = new java.util.LinkedHashMap<>();
        Arrays.stream(response.getHeaders()).forEach(h -> headers.put(h.getName(), h.getValue()));
        return headers;
    }

    private void validate(ApiRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Request payload is required");
        }
        if (dto.getMethod() == null || !ALLOWED_METHODS.contains(dto.getMethod().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Unsupported HTTP method");
        }
        if (dto.getUrl() == null || dto.getUrl().isBlank()) {
            throw new IllegalArgumentException("URL is required");
        }
        if (dto.getBody() != null && dto.getBody().getBytes(StandardCharsets.UTF_8).length > MAX_BODY_SIZE) {
            throw new IllegalArgumentException("Request body exceeds size limit");
        }

        URI uri = URI.create(dto.getUrl());
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("URL host is invalid");
        }
        if (isLocalhost(host)) {
            throw new IllegalArgumentException("Localhost calls are blocked");
        }
    }

    private boolean isLocalhost(String host) {
        String normalized = host.toLowerCase(Locale.ROOT);
        if ("localhost".equals(normalized) || "127.0.0.1".equals(normalized) || "::1".equals(normalized) || "0.0.0.0".equals(normalized)) {
            return true;
        }
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isLoopbackAddress() || address.isAnyLocalAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
