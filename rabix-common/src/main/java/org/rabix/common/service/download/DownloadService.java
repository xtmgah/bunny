package org.rabix.common.service.download;

import java.io.File;
import java.util.Map;
import java.util.Set;

public interface DownloadService {

  void download(File workingDir, String path, Map<String, Object> config) throws DownloadServiceException;
  
  void download(File workingDir, Set<String> paths, Map<String, Object> config) throws DownloadServiceException;
  
}
