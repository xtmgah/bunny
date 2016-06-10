package org.rabix.engine.rest.backend.stub;

import org.rabix.bindings.model.Job;
import org.rabix.transport.backend.Backend;

public interface BackendStub {

  void start();
  
  void stop();

  void send(Job job);
  
  Backend getBackend();

}
