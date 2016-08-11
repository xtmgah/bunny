package org.rabix.common.service.upload.impl;

import java.io.File;

import org.rabix.common.service.upload.UploadService;
import org.rabix.common.service.upload.UploadServiceException;

public class NoOpUploadServiceImpl implements UploadService {

  @Override
  public void upload(File file, String remotePath) throws UploadServiceException {
    // do nothing
  }

}
