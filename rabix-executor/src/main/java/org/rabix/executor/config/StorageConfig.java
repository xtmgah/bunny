package org.rabix.executor.config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.InternalSchemaHelper;

public class StorageConfig {

  public static enum BackendStore {
    LOCAL,
    FTP
  }
  
  public static File getWorkingDir(Job job, Configuration configuration) {
    File baseDir = new File(getLocalExecutionDirectory(configuration));
    File contextDir = new File(baseDir, job.getRootId());
    if (!contextDir.exists()) {
      contextDir.mkdirs();
    }
    
    File workingDir = contextDir;
    String[] idArray = transformLocalIDsToPath(job);

    for (String id : idArray) {
      workingDir = new File(workingDir, sanitize(id));
      if (!workingDir.exists()) {
        workingDir.mkdirs();
      }
    }
    return workingDir;
  }
  
  public static File getWorkingDirWithoutBase(Job job, Configuration configuration) {
    File contextDir = new File(job.getRootId());
    if (!contextDir.exists()) {
      contextDir.mkdirs();
    }
    
    File workingDir = contextDir;
    String[] idArray = transformLocalIDsToPath(job);

    for (String id : idArray) {
      workingDir = new File(workingDir, sanitize(id));
      if (!workingDir.exists()) {
        workingDir.mkdirs();
      }
    }
    return workingDir;
  }
  
  public static File getWorkingDirWithoutRootId(Job job, Configuration configuration) {
    File workingDir = new File("/");
    String[] idArray = transformLocalIDsToPath(job);

    for (String id : idArray) {
      workingDir = new File(workingDir, sanitize(id));
      if (!workingDir.exists()) {
        workingDir.mkdirs();
      }
    }
    return workingDir;
  }
  
  private static String[] transformLocalIDsToPath(Job job) {
    String nodeId = job.getName();
    return nodeId.split("\\" + InternalSchemaHelper.SEPARATOR);
  }
  
  public static String getLocalExecutionDirectory(Configuration configuration) {
    String backendExecutionDirectory = configuration.getString("backend.execution.directory");
    try {
      return new File(backendExecutionDirectory).getCanonicalPath();
    } catch (IOException e) {
      throw new RuntimeException("Failed to get backend.execution.directory.");
    }
  }
  
  public static boolean isDockerSupported(Configuration configuration) {
    return configuration.getBoolean("backend.docker.enabled", false);
  }
  
  public static BackendStore getBackendStore(Configuration configuration) {
    String backendStore = configuration.getString("backend.store");
    if (backendStore == null || backendStore.isEmpty()) {
      backendStore = BackendStore.LOCAL.name();
    }
    
    for (BackendStore backendStoreEnum : BackendStore.values()) {
      if (backendStore.trim().equalsIgnoreCase(backendStoreEnum.name())) {
        return backendStoreEnum;
      }
    }
    throw new RuntimeException("Invalid backend.store value " + backendStore);
  }
  
  /**
   * Normalize application ID
   */
  private static String sanitize(String id) {
    id = id.replace("@", "_");
    id = id.replace("/", "_");
    id = id.replace("^", "_");
    id = id.replace(":", "_");
    return id.replaceAll("_+", "_");
  }
  
}