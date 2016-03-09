package org.rabix.executor.service.impl;

import java.io.File;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.FileValue;
import org.rabix.executor.config.StorageConfig;
import org.rabix.executor.config.StorageConfig.BackendStore;
import org.rabix.executor.service.DownloadFileService;
import org.rabix.ftp.SimpleFTPClient;

import com.google.inject.Inject;

public class DownloadServiceImpl implements DownloadFileService {

  private final SimpleFTPClient ftpClient;
  private final Configuration configuration;
  
  @Inject
  public DownloadServiceImpl(Configuration configuration, SimpleFTPClient ftpClient) {
    this.configuration = configuration;
    this.ftpClient = ftpClient;
  }
  
  @Override
  public void download(final Executable executable, final Set<FileValue> fileValues) throws Exception {
    for (FileValue fileValue : fileValues) {
      File file = new File(new File(StorageConfig.getLocalExecutionDirectory(configuration)), fileValue.getPath());
      if (file.exists()) {
        continue;
      }
      if (StorageConfig.getBackendStore(configuration).equals(BackendStore.FTP)) {
        ftpClient.download(new File(StorageConfig.getLocalExecutionDirectory(configuration)), fileValue.getPath());
      }
    }
  }
  
}
