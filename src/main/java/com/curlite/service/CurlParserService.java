package com.curlite.service;

import com.curlite.dto.ApiRequestDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CurlParserService {

    public ApiRequestDTO parse(String curlCommand) {
        if (curlCommand == null || curlCommand.isBlank()) {
            throw new IllegalArgumentException("curl command is required");
        }

        List<String> tokens = tokenize(curlCommand);
        ApiRequestDTO dto = new ApiRequestDTO();
        dto.setMethod("GET");

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            String lower = token.toLowerCase(Locale.ROOT);

            if (("-x".equals(lower) || "--request".equals(lower)) && i + 1 < tokens.size()) {
                dto.setMethod(tokens.get(++i).toUpperCase(Locale.ROOT));
            } else if (("-h".equals(lower) || "--header".equals(lower)) && i + 1 < tokens.size()) {
                String header = tokens.get(++i);
                int idx = header.indexOf(":");
                if (idx > 0) {
                    dto.getHeaders().put(header.substring(0, idx).trim(), header.substring(idx + 1).trim());
                }
            } else if (("-d".equals(lower) || "--data".equals(lower) || "--data-raw".equals(lower)) && i + 1 < tokens.size()) {
                dto.setBody(tokens.get(++i));
                if ("GET".equalsIgnoreCase(dto.getMethod())) {
                    dto.setMethod("POST");
                }
            } else if (token.startsWith("http://") || token.startsWith("https://")) {
                dto.setUrl(token);
            }
        }

        if (dto.getUrl() == null || dto.getUrl().isBlank()) {
            throw new IllegalArgumentException("Unable to detect URL in curl command");
        }

        return dto;
    }

    private List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean single = false;
        boolean dbl = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\'' && !dbl) {
                single = !single;
                continue;
            }
            if (c == '"' && !single) {
                dbl = !dbl;
                continue;
            }
            if (Character.isWhitespace(c) && !single && !dbl) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        return tokens;
    }
}
