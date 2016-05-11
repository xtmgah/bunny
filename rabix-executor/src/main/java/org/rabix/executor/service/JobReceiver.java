package org.rabix.executor.service;

import org.rabix.bindings.model.Job;

public interface JobReceiver {

  void stop();
  
  void start();
  
  Job receive();
  
}
