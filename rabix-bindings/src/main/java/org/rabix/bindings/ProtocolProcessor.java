package org.rabix.bindings;

import java.io.File;

import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.Job;

public interface ProtocolProcessor {

  Job preprocess(final Job job, final File workingDir) throws BindingException;
  
  Job mapInputFilePaths(final Job job, final FileMapper fileMapper) throws BindingException;
  
  Job mapOutputFilePaths(final Job job, final FileMapper fileMapper) throws BindingException;
  
}
