package org.rabix.executor.handler;

import org.rabix.bindings.model.Job;
import org.rabix.executor.engine.EngineStub;

public interface JobHandlerFactory {

  JobHandler createHandler(Job job, EngineStub engineStub);
  
}
