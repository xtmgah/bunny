package org.rabix.common.service.download.impl;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.rabix.common.service.download.DownloadService;
import org.rabix.common.service.download.DownloadServiceException;

public class NoOpDownloadServiceImpl implements DownloadService {

  @Override
  public void download(File workingDir, String path, Map<String, Object> config) throws DownloadServiceException {
    // do nothing
  }

  @Override
  public void download(File workingDir, Set<String> paths, Map<String, Object> config) throws DownloadServiceException {
    // do nothing
  }

}
