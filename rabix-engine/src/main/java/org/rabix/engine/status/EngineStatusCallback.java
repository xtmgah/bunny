package org.rabix.engine.status;

import org.rabix.bindings.model.Job;

public interface EngineStatusCallback {

  void onJobReady(Job job) throws EngineStatusCallbackException;

  void onJobFailed(Job job) throws EngineStatusCallbackException;

  void onJobRootCompleted(Job rootJob) throws EngineStatusCallbackException;
  
  void onJobRootPartiallyCompleted(Job rootJob) throws EngineStatusCallbackException;
  
  void onJobRootFailed(Job rootJob) throws EngineStatusCallbackException;

}
