package org.rabix.executor.rest.status;

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
  public void onContainerImagePullStarted(Job job, String image) throws ExecutorStatusCallbackException {
    logger.debug("onContainerImagePullStarted(jobId={}, image={})", job.getId(), image);
  }

  @Override
  public void onContainerImagePullCompleted(Job job, String image) throws ExecutorStatusCallbackException {
    logger.debug("onContainerImagePullCompleted(jobId={}, image={})", job.getId(), image);
  }

  @Override
  public void onInputFilesDownloadStarted(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onInputFilesDownloadStarted(jobId={})", job.getId());
  }

  @Override
  public void onInputFilesDownloadCompleted(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onInputFilesDownloadCompleted(jobId={})", job.getId());
  }

  @Override
  public void onOutputFilesUploadStarted(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onOutputFilesUploadStarted(jobId={})", job.getId());
  }

  @Override
  public void onOutputFilesUploadCompleted(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onOutputFilesUploadCompleted(jobId={})", job.getId());
  }

  @Override
  public void onJobStopped(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onJobStopped(jobId={})", job.getId());
  }

  @Override
  public void onContainerImagePullFailed(Job job, String image) throws ExecutorStatusCallbackException {
    logger.debug("onContainerImagePullFailed(jobId={}, image={})", job.getId(), image);
  }

  @Override
  public void onInputFilesDownloadFailed(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onInputFilesDownloadFailed(jobId={})", job.getId());
  }

  @Override
  public void onOutputFilesUploadFailed(Job job) throws ExecutorStatusCallbackException {
    logger.debug("onOutputFilesUploadFailed(jobId={})", job.getId());
  }

}
