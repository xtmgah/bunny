package org.rabix.bindings.cwl1.service;

import java.io.File;
import java.util.Set;

import org.rabix.bindings.cwl1.bean.CWL1Job;

public interface CWL1GlobService {

  Set<File> glob(CWL1Job job, File workingDir, Object glob) throws CWL1GlobException;
  
}
