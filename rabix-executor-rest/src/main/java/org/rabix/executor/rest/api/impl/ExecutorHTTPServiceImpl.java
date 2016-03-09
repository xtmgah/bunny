package org.rabix.executor.rest.api.impl;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.Executable.ExecutableStatus;
import org.rabix.executor.WorkerStatus;
import org.rabix.executor.rest.api.ExecutorHTTPService;
import org.rabix.executor.service.ExecutorService;

import com.google.inject.Inject;

public class ExecutorHTTPServiceImpl implements ExecutorHTTPService {

  private final ExecutorService executorService;

  @Inject
  public ExecutorHTTPServiceImpl(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public Response startExecutable(Executable executable, HttpHeaders headers) {
    String contextId = headers.getHeaderString(CONTEXT_ID);
    executorService.start(executable, contextId);
    return ok();
  }

  public Response stopExecutable(String id, HttpHeaders headers) {
    String contextId = headers.getHeaderString(CONTEXT_ID);
    executorService.stop(id, contextId);
    return ok();
  }

  public Response executableStatus(String id, HttpHeaders headers) {
    String contextId = headers.getHeaderString(CONTEXT_ID);
    ExecutableStatus status = executorService.findStatus(id, contextId);
    if (status == null) {
      throw new WebApplicationException(404);
    }
    return ok(status);
  }

  public Response executableResult(String id, HttpHeaders headers) {
    String contextId = headers.getHeaderString(CONTEXT_ID);
    return ok(executorService.getResult(id, contextId));
  }

  public Response workerStatus() {
    if (executorService.isStopped()) {
      return ok(WorkerStatus.STOPPED);
    }
    return ok(WorkerStatus.RUNNING);
  }

  public Response shutdown() {
    executorService.shutdown(false);
    return ok();
  }

  public Response shutdownNow() {
    executorService.shutdown(true);
    return ok();
  }

  public Response ok(Object items) {
    if (items == null) {
      return ok();
    }
    return Response.ok().entity(items).build();
  }

  public Response ok() {
    return Response.ok().build();
  }

}
