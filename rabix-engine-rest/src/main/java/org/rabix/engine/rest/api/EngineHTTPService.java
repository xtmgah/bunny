package org.rabix.engine.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rabix.engine.rest.dto.Task;

@Produces(MediaType.APPLICATION_JSON)
@Path("/v0/engine")
public interface EngineHTTPService {

  @POST
  Response start(Task task);
  
  @GET
  @Path("/{id}")
  Response get(@PathParam("id") String id);
  
  @GET
  Response get();

}
