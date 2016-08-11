package org.rabix.common.service.download.impl;

import java.io.File;

import org.rabix.common.service.download.DownloadService;
import org.rabix.common.service.download.DownloadServiceException;

public class NoOpDownloadServiceImpl implements DownloadService {

  @Override
  public void download(File workingDir, String remotePath) throws DownloadServiceException {
    // do nothing
  }

}
