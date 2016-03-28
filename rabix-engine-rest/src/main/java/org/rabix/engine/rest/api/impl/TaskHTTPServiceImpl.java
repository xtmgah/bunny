package org.rabix.engine.rest.api.impl;

import java.util.Collections;

import javax.ws.rs.core.Response;

import org.rabix.engine.rest.api.TaskHTTPService;
import org.rabix.engine.rest.dto.Task;
import org.rabix.engine.rest.service.impl.TaskServiceImpl;

import com.google.inject.Inject;

import ch.qos.logback.core.status.Status;

public class TaskHTTPServiceImpl implements TaskHTTPService {

  private final TaskServiceImpl taskService;

  @Inject
  public TaskHTTPServiceImpl(TaskServiceImpl taskService) {
    this.taskService = taskService;
  }
  
  @Override
  public Response create(Task task) {
    try {
      return ok(taskService.create(task));
    } catch (Exception e) {
      return error();
    }
  }
  
  @Override
  public Response get() {
    return ok(taskService.get());
  }
  
  @Override
  public Response get(String id) {
    Task task = taskService.get(id);
    if (task == null) {
      return error();
    }
    return ok(task);
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
    return Response.ok(Collections.emptyMap()).build();
  }

}
