package org.rabix.engine.rest.service;

import java.util.Set;

import org.rabix.engine.rest.dto.Task;

public interface TaskService {

  String create(Task task) throws TaskServiceException;

  Set<Task> get();
  
  public Task get(String id);
  
}
