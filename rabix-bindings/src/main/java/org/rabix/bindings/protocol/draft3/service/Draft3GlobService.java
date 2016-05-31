package org.rabix.bindings.protocol.draft3.service;

import java.io.File;
import java.util.Set;

import org.rabix.bindings.protocol.draft3.bean.Draft3Job;

public interface Draft3GlobService {

  Set<File> glob(Draft3Job job, File workingDir, Object glob) throws Draft3GlobException;
  
}
