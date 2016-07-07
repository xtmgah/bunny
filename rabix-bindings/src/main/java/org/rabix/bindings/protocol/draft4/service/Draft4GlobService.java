package org.rabix.bindings.protocol.draft4.service;

import java.io.File;
import java.util.Set;

import org.rabix.bindings.protocol.draft4.bean.Draft4Job;

public interface Draft4GlobService {

  Set<File> glob(Draft4Job job, File workingDir, Object glob) throws Draft4GlobException;
  
}
