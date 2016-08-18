package org.rabix.executor.config;

import java.io.File;

import org.rabix.bindings.model.Job;

public interface StorageConfiguration {

  public static enum BackendStore {
    VAPOR,
    LOCAL,
    FTP
  }

  File getWorkingDir(Job job);
  
  File getWorkingDirWithoutRoot(Job job);
  
  File getLocalExecutionDirectory();
  
  BackendStore getBackendStore();

}