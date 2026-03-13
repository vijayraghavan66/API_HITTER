package com.curlite.resource;

import com.curlite.dto.ApiRequestDTO;
import com.curlite.service.CurlParserService;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/curl/parse")
@Produces(MediaType.APPLICATION_JSON)
public class CurlParserResource {

    private final CurlParserService curlParserService = new CurlParserService();

    @POST
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public Response parse(String body) {
        String payload = body;
        if (payload != null && payload.trim().startsWith("{") && payload.contains("\"curl\"")) {
            payload = extractCurlValue(payload);
        }
        try {
            ApiRequestDTO parsed = curlParserService.parse(payload);
            return Response.ok(parsed).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    private String extractCurlValue(String json) {
        int keyIndex = json.indexOf("\"curl\"");
        if (keyIndex < 0) {
            return json;
        }
        int colon = json.indexOf(':', keyIndex);
        int firstQuote = json.indexOf('"', colon + 1);
        int lastQuote = json.lastIndexOf('"');
        if (firstQuote < 0 || lastQuote <= firstQuote) {
            return json;
        }
        return json.substring(firstQuote + 1, lastQuote)
            .replace("\\n", " ")
            .replace("\\\"", "\"")
            .trim();
    }
}
