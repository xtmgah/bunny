package org.rabix.common.service.upload.impl;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.rabix.common.service.upload.UploadService;
import org.rabix.common.service.upload.UploadServiceException;

public class NoOpUploadServiceImpl implements UploadService {

  @Override
  public void upload(File file, File baseExecutionDirectory, boolean wait, boolean create, Map<String, Object> config) throws UploadServiceException {
    // do nothing
  }

  @Override
  public void upload(Set<File> files, File baseExecutionDirectory, boolean wait, boolean create, Map<String, Object> config) throws UploadServiceException {
    // do nothing
  }

}
