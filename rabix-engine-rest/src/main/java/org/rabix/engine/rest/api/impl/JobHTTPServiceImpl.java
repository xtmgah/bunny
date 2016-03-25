package org.rabix.engine.rest.api.impl;

import java.util.Collections;

import javax.ws.rs.core.Response;

import org.rabix.bindings.model.Job;
import org.rabix.engine.rest.api.JobHTTPService;
import org.rabix.engine.rest.service.JobServiceException;
import org.rabix.engine.rest.service.JobService;

import com.google.inject.Inject;

import ch.qos.logback.core.status.Status;

public class JobHTTPServiceImpl implements JobHTTPService {

  private final JobService jobService;

  @Inject
  public JobHTTPServiceImpl(JobService jobService) {
    this.jobService = jobService;
  }
  
  @Override
  public Response save(String id, Job job) {
    try {
      jobService.update(job);
    } catch (JobServiceException e) {
      return error();
    }
    return ok();
  }
  
  private Response error() {
    return Response.status(Status.ERROR).build();
  }
  
  private Response ok() {
    return Response.ok(Collections.emptyMap()).build();
  }
}
