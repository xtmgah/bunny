package org.rabix.engine.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rabix.bindings.model.Executable;
import org.rabix.engine.rest.dto.Task;

@Produces(MediaType.APPLICATION_JSON)
@Path("/v0/engine")
public interface EngineHTTPService {

  @PUT
  @Path("/jobs/{id}")
  Response updateExecutable(@PathParam("id") String id, Executable executable);
  
  @POST
  @Path("/tasks")
  Response createTask(Task task);
  
  @GET
  @Path("/tasks")
  Response getTasks();
  
  @GET
  @Path("/tasks/{id}")
  public Response getTask(@PathParam("id")  String id);
}
