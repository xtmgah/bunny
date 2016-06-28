package org.rabix.engine.rest.backend.stub;

import java.util.Map;

import org.rabix.bindings.model.Job;
import org.rabix.common.engine.control.EngineControlMessage;
import org.rabix.transport.backend.Backend;

public interface BackendStub {

  void start(final Map<String, Long> heartbeatInfo);
  
  void stop();

  void send(Job job);
  
  void send(EngineControlMessage controlMessage);
  
  Backend getBackend();

}
