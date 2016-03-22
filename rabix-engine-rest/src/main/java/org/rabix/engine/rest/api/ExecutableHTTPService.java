package org.rabix.engine.rest.api;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rabix.bindings.model.Executable;

@Produces(MediaType.APPLICATION_JSON)
@Path("/v0/engine/jobs")
public interface ExecutableHTTPService {

  @PUT
  @Path("/{id}")
  Response save(@PathParam("id") String id, Executable executable);
  
}
