package org.rabix.executor.service;

import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.Executable.ExecutableStatus;

public interface ExecutorService {

  void start(final Executable executable, String contextId);

  void stop(String id, String contextId);

  void shutdown(Boolean stopEverything);

  boolean isRunning(String id, String contextId);
  
  Object getResult(String id, String contextId);
  
  boolean isStopped();

  ExecutableStatus findStatus(String id, String contextId);
  
}
