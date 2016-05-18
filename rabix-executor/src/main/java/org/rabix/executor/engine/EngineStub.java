package org.rabix.executor.engine;

import org.rabix.bindings.model.Job;

public interface EngineStub {

  void start();

  void stop();

  void send(Job job);

}
