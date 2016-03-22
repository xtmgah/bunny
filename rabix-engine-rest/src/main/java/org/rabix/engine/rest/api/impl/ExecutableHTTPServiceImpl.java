package org.rabix.engine.rest.api.impl;

import java.util.Collections;

import javax.ws.rs.core.Response;

import org.rabix.bindings.model.Executable;
import org.rabix.engine.rest.api.ExecutableHTTPService;
import org.rabix.engine.rest.service.ExecutableServiceException;
import org.rabix.engine.rest.service.impl.ExecutableServiceImpl;

import com.google.inject.Inject;

import ch.qos.logback.core.status.Status;

public class ExecutableHTTPServiceImpl implements ExecutableHTTPService {

  private final ExecutableServiceImpl executableService;

  @Inject
  public ExecutableHTTPServiceImpl(ExecutableServiceImpl executableService) {
    this.executableService = executableService;
  }
  
  @Override
  public Response save(String id, Executable executable) {
    try {
      executableService.update(executable);
    } catch (ExecutableServiceException e) {
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
