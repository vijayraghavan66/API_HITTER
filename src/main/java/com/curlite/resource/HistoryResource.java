package com.curlite.resource;

import com.curlite.model.History;
import com.curlite.service.HistoryService;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/history")
@Produces(MediaType.APPLICATION_JSON)
public class HistoryResource {

    private final HistoryService historyService = new HistoryService();

    @GET
    public Response getHistory() {
        List<History> entries = historyService.getRecent();
        return Response.ok(entries).build();
    }
}
