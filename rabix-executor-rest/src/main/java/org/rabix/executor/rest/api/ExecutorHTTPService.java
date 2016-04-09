package org.rabix.executor.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Path("/v0/executor")
public interface ExecutorHTTPService {

  public static final String CONTEXT_ID = "context-id";
  
  @GET
  @Path("/executor_status")
  Response status();

  @POST
  @Path("/shutdown")
  Response shutdown();

  @POST
  @Path("/shutdown_now")
  Response shutdownNow();

}
