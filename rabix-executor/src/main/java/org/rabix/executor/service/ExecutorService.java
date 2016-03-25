package org.rabix.executor.service;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;

public interface ExecutorService {

  void start(final Job job, String contextId);

  void stop(String id, String contextId);

  void shutdown(Boolean stopEverything);

  boolean isRunning(String id, String contextId);
  
  Object getResult(String id, String contextId);
  
  boolean isStopped();

  JobStatus findStatus(String id, String contextId);
  
}
