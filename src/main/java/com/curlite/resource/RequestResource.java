package com.curlite.resource;

import com.curlite.dto.ApiRequestDTO;
import com.curlite.dto.ApiResponseDTO;
import com.curlite.service.RequestExecutionService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/execute")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RequestResource {

    private final RequestExecutionService requestExecutionService = new RequestExecutionService();

    @POST
    public Response execute(ApiRequestDTO requestDTO) {
        try {
            ApiResponseDTO response = requestExecutionService.execute(requestDTO);
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            ApiResponseDTO response = new ApiResponseDTO();
            response.setStatus(400);
            response.setError(e.getMessage());
            response.setBody("");
            response.setResponseTime(0);
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }
    }
}
