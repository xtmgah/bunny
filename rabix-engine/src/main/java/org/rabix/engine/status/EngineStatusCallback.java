package org.rabix.engine.status;

import org.rabix.bindings.model.Job;

public interface EngineStatusCallback {

  void onJobReady(Job job) throws EngineStatusCallbackException;

  void onJobFailed(Job job) throws EngineStatusCallbackException;

  void onJobRootCompleted(String string) throws EngineStatusCallbackException;

  void onJobRootFailed(String string) throws EngineStatusCallbackException;

}
