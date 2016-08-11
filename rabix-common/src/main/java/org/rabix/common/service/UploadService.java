package org.rabix.common.service;

import java.io.File;

public interface UploadService {

  void upload(File file, String remotePath) throws UploadServiceException;
  
}
