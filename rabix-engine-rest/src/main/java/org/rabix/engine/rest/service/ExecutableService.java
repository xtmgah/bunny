package org.rabix.engine.rest.service;

import java.util.List;

import org.rabix.bindings.model.Executable;
import org.rabix.engine.processor.EventProcessor;

public interface ExecutableService {

  void update(Executable executable) throws ExecutableServiceException;
  
  List<Executable> getReady(EventProcessor eventProcessor, String contextId) throws ExecutableServiceException;
  
}
