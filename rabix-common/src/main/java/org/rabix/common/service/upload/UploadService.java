package org.rabix.common.service.upload;

import java.io.File;
import java.util.Map;
import java.util.Set;

public interface UploadService {

  void upload(File file, File executionDirectory, boolean wait, boolean create, Map<String, Object> config) throws UploadServiceException;
  
  void upload(Set<File> files, File executionDirectory, boolean wait, boolean create, Map<String, Object> config) throws UploadServiceException;
  
}
