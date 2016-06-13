package org.rabix.engine.processor;

import org.rabix.bindings.model.Job;

public interface JobCallback {

  void onReady(Job job) throws Exception;
  
  void onRootCompleted(String rootId) throws Exception;
  
  void onRootFailed(String rootId) throws Exception;
  
}
