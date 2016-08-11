package org.rabix.common.service.download;

import java.io.File;

public interface DownloadService {

  void download(File workingDir, String remotePath) throws DownloadServiceException;
  
}
