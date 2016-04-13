package org.rabix.engine.rest.backend;

import java.util.Set;

import org.rabix.bindings.model.Job;
import org.rabix.engine.rest.model.Backend;

public interface BackendStub {

  void start();
  
  void stop();

  void send(Job job);
  
  void send(Set<Job> jobs);
  
  HeartbeatInfo getHeartbeat();
  
  Backend getBackend();

}
