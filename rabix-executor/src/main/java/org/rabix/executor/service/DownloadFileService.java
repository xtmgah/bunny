package org.rabix.executor.service;

import java.util.Set;

import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.FileValue;

public interface DownloadFileService {

  void download(final Executable executable, final Set<FileValue> fileValues) throws Exception;
}
