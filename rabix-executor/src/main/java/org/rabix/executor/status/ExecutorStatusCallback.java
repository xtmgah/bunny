package org.rabix.executor.status;

import org.rabix.bindings.model.Job;

public interface ExecutorStatusCallback {

  void onJobFailed(Job job) throws ExecutorStatusCallbackException;
  
  void onJobStarted(Job job) throws ExecutorStatusCallbackException;
  
  void onJobCompleted(Job job) throws ExecutorStatusCallbackException;
  
  void onContainerImagePullStarted(String image) throws ExecutorStatusCallbackException;
  
  void onContainerImagePullCompleted(String image) throws ExecutorStatusCallbackException;
  
  void onInputFilesDownloadStarted() throws ExecutorStatusCallbackException;
  
  void onInputFilesDownloadCompleted() throws ExecutorStatusCallbackException;
  
  void onOutputFilesUploadStarted() throws ExecutorStatusCallbackException;
  
  void onOutputFilesUploadComplted() throws ExecutorStatusCallbackException;
  
}
