package org.rabix.bindings.protocol.draft2.service;

import java.io.File;
import java.util.Set;

import org.rabix.bindings.protocol.draft2.bean.Draft2Job;

public interface Draft2GlobService {

  Set<File> glob(Draft2Job job, File workingDir, Object glob) throws Draft2GlobException;
  
}
