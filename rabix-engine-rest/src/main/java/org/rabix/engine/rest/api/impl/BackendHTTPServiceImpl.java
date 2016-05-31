package org.rabix.engine.rest.api.impl;

import java.util.Collections;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.rabix.engine.rest.api.BackendHTTPService;
import org.rabix.engine.rest.service.BackendService;
import org.rabix.transport.backend.Backend;

import com.google.inject.Inject;

public class BackendHTTPServiceImpl implements BackendHTTPService {

  private final BackendService backendService;
  
  @Inject
  public BackendHTTPServiceImpl(BackendService backendService) {
    this.backendService = backendService;
  }
  
  @Override
  public Response create(Backend backend) {
    try {
      return ok(backendService.create(backend));
    } catch (Exception e) {
      return error();
    }
  }
  
  private Response error() {
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }
  
  private Response ok() {
    return Response.ok(Collections.emptyMap()).build();
  }
  
  private Response ok(Object items) {
    if (items == null) {
      return ok();
    }
    return Response.ok().entity(items).build();
  }
  
}
