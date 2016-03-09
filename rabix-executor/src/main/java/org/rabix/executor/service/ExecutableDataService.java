package org.rabix.executor.service;

import java.util.List;

import org.rabix.bindings.model.Executable.ExecutableStatus;
import org.rabix.executor.model.ExecutableData;

public interface ExecutableDataService {

  void save(ExecutableData data, String contextId);

  void save(ExecutableData executableData, String message, ExecutableStatus status, String contextId);
  
  public ExecutableData find(String id, String contextId);

  public List<ExecutableData> find(ExecutableStatus... statuses);
  
}
