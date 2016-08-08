package org.rabix.bindings.sb.service;

import java.io.File;
import java.util.Set;

import org.rabix.bindings.sb.bean.SBJob;

public interface SBGlobService {

  Set<File> glob(SBJob job, File workingDir, Object glob) throws SBGlobException;
  
}
