package org.rabix.executor.status;

import org.rabix.bindings.model.Job;

public interface ExecutorStatusCallback {

  void onJobReady(Job job) throws ExecutorStatusCallbackException;
  
  void onJobFailed(Job job) throws ExecutorStatusCallbackException;
  
  void onJobStarted(Job job) throws ExecutorStatusCallbackException;
  
  void onJobStopped(Job job) throws ExecutorStatusCallbackException;
  
  void onJobCompleted(Job job) throws ExecutorStatusCallbackException;
  
  void onContainerImagePullStarted(Job job, String image) throws ExecutorStatusCallbackException;
  
  void onContainerImagePullFailed(Job job, String image) throws ExecutorStatusCallbackException;
  
  void onContainerImagePullCompleted(Job job, String image) throws ExecutorStatusCallbackException;
  
  void onInputFilesDownloadStarted(Job job) throws ExecutorStatusCallbackException;
  
  void onInputFilesDownloadFailed(Job job) throws ExecutorStatusCallbackException;
  
  void onInputFilesDownloadCompleted(Job job) throws ExecutorStatusCallbackException;
  
  void onOutputFilesUploadStarted(Job job) throws ExecutorStatusCallbackException;
  
  void onOutputFilesUploadFailed(Job job) throws ExecutorStatusCallbackException;
  
  void onOutputFilesUploadCompleted(Job job) throws ExecutorStatusCallbackException;
  
}
