package org.rabix.engine.rest.api.impl;

import java.util.HashMap;

import javax.ws.rs.core.Response;

import org.rabix.bindings.model.Executable;
import org.rabix.engine.rest.api.EngineHTTPService;
import org.rabix.engine.rest.dto.Task;
import org.rabix.engine.rest.service.ExecutableService;
import org.rabix.engine.rest.service.ExecutableServiceException;
import org.rabix.engine.rest.service.TaskService;

import com.google.inject.Inject;

import ch.qos.logback.core.status.Status;

public class EngineHTTPServiceImpl implements EngineHTTPService {

  private final TaskService taskService;
  private final ExecutableService executableService;

  @Inject
  public EngineHTTPServiceImpl(TaskService taskService, ExecutableService executableService) {
    this.taskService = taskService;
    this.executableService = executableService;
  }
  
  @Override
  public Response createTask(Task task) {
    try {
      return ok(taskService.create(task));
    } catch (Exception e) {
      return error();
    }
  }
  
  @Override
  public Response getTasks() {
    return ok(taskService.get());
  }
  
  @Override
  public Response getTask(String id) {
    Task task = taskService.get(id);
    if (task == null) {
      return error();
    }
    return ok(task);
  }
  
  @Override
  public Response updateExecutable(String id, Executable executable) {
    try {
      executableService.updateExecutable(executable);
    } catch (ExecutableServiceException e) {
      return error();
    }
    return ok();
  }
  
  private Response error() {
    return Response.status(Status.ERROR).build();
  }
  
  private Response ok(Object items) {
    if (items == null) {
      return ok();
    }
    return Response.ok().entity(items).build();
  }

  private Response ok() {
    return Response.ok(new HashMap<>()).build();
  }

}
