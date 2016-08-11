package org.rabix.engine.status.impl;

import org.rabix.bindings.model.Job;
import org.rabix.engine.status.EngineStatusCallback;
import org.rabix.engine.status.EngineStatusCallbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpEngineStatusCallback implements EngineStatusCallback {

  private final static Logger logger = LoggerFactory.getLogger(NoOpEngineStatusCallback.class);
  
  @Override
  public void onJobReady(Job job) throws EngineStatusCallbackException {
    logger.debug("onJobReady(jobId={})", job.getId());
  }

  @Override
  public void onJobFailed(Job job) throws EngineStatusCallbackException {
    logger.debug("onJobFailed(jobId={})", job.getId());
  }

  @Override
  public void onJobRootCompleted(String rootId) throws EngineStatusCallbackException {
    logger.debug("onJobRootCompleted(rootId={})", rootId);
  }

  @Override
  public void onJobRootFailed(String rootId) throws EngineStatusCallbackException {
    logger.debug("onJobFailed(rootId)", rootId);
  }

}
