package org.rabix.common.service;

import java.io.File;

public interface DownloadService {

  void download(File workingDir, String remotePath) throws DownloadServiceException;
  
}
