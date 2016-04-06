package org.rabix.executor.service;

import java.util.Set;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.FileValue;

public interface DownloadFileService {

  void download(final Job job, final Set<FileValue> fileValues) throws Exception;
}
