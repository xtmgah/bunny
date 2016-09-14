package org.rabix.executor.service;

import org.rabix.bindings.model.Job;
import org.rabix.executor.container.ContainerException;

public interface FilePermissionService {

  void execute(Job job) throws ContainerException;
  
}
