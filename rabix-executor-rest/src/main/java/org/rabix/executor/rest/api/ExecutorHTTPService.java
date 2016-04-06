package org.rabix.executor.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rabix.bindings.model.Job;

@Produces(MediaType.APPLICATION_JSON)
@Path("/v0/executor")
public interface ExecutorHTTPService {

  public static final String CONTEXT_ID = "context-id";
  
  @POST
  @Path("/start_job")
  Response startJob(Job job, @Context HttpHeaders headers);

  @POST
  @Path("/stop_job/")
  Response stopJob(@QueryParam("id") String id, @Context HttpHeaders headers);

  @GET
  @Path("/job_status")
  Response jobStatus(@QueryParam("id") String id, @Context HttpHeaders headers);
  
  @GET
  @Path("/job_result")
  Response jobResult(@QueryParam("id") String id, @Context HttpHeaders headers);

  @GET
  @Path("/executor_status")
  Response workerStatus();

  @POST
  @Path("/shutdown")
  Response shutdown();

  @POST
  @Path("/shutdown_now")
  Response shutdownNow();

}
