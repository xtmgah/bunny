package org.rabix.executor.handler;

import org.rabix.bindings.model.Job;

public interface JobHandlerFactory {

  JobHandler createHandler(Job job);
  
}
