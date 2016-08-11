package org.rabix.backend.local.status;

import org.rabix.bindings.model.Job;
import org.rabix.executor.status.ExecutorStatusCallback;
import org.rabix.executor.status.ExecutorStatusCallbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpExecutorStatusCallback implements ExecutorStatusCallback {

  private final static Logger logger = LoggerFactory.getLogger(NoOpExecutorStatusCallback.class);

  @Override
  public void onJobFailed(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onJobFailed(jobId={})", job.getId());
  }

  @Override
  public void onJobStarted(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onJobStarted(jobId={})", job.getId());
  }

  @Override
  public void onJobCompleted(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onJobCompleted(jobId={})", job.getId());
  }

  @Override
  public void onContainerImagePullStarted(String image) throws ExecutorStatusCallbackException {
    logger.debug("onContainerImagePullStarted(image={})", image);
  }

  @Override
  public void onContainerImagePullCompleted(String image) throws ExecutorStatusCallbackException {
    logger.debug("onContainerImagePullCompleted(image={})", image);
  }

  @Override
  public void onInputFilesDownloadStarted() throws ExecutorStatusCallbackException {
    logger.debug("onInputFilesDownloadStarted()");
  }

  @Override
  public void onInputFilesDownloadCompleted() throws ExecutorStatusCallbackException {
    logger.debug("onInputFilesDownloadCompleted()");
  }

  @Override
  public void onOutputFilesUploadStarted() throws ExecutorStatusCallbackException {
    logger.debug("onOutputFilesUploadStarted()");
  }

  @Override
  public void onOutputFilesUploadComplted() throws ExecutorStatusCallbackException {
    logger.debug("onOutputFilesUploadComplted()");
  }

}
