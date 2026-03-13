package com.curlite.resource;

import com.curlite.model.Collection;
import com.curlite.service.CollectionService;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/collections")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CollectionResource {

    private final CollectionService collectionService = new CollectionService();

    @POST
    public Response create(Collection collection) {
        Collection created = collectionService.createCollection(collection == null ? new Collection() : collection);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    public Response getAll() {
        List<Collection> collections = collectionService.getCollections();
        return Response.ok(collections).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") long id) {
        Optional<Collection> collection = collectionService.getCollection(id);
        return collection.map(value -> Response.ok(value).build())
            .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") long id) {
        boolean deleted = collectionService.deleteCollection(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }
}
