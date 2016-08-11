package org.rabix.engine.status;

import org.rabix.bindings.model.Job;

public interface EngineStatusCallback {

  void onJobReady(Job job) throws EngineStatusCallbackException;

  void onJobFailed(Job job) throws EngineStatusCallbackException;

  void onJobRootCompleted(String rootId) throws EngineStatusCallbackException;

  void onJobRootFailed(String rootId) throws EngineStatusCallbackException;

}
