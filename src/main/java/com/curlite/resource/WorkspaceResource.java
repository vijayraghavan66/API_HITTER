package com.curlite.resource;

import com.curlite.model.Workspace;
import com.curlite.service.WorkspaceService;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/workspaces")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WorkspaceResource {

    private final WorkspaceService workspaceService = new WorkspaceService();

    @GET
    public Response list() {
        List<Workspace> workspaces = workspaceService.list();
        return Response.ok(workspaces).build();
    }

    @POST
    public Response create(Workspace workspace) {
        Workspace created = workspaceService.create(workspace);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        Optional<Workspace> workspace = workspaceService.get(id);
        return workspace.map(value -> Response.ok(value).build())
            .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id, Workspace workspace) {
        Optional<Workspace> updated = workspaceService.update(id, workspace == null ? new Workspace() : workspace);
        return updated.map(value -> Response.ok(value).build())
            .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }
}
