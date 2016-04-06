package org.rabix.bindings;

import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.Job;

public interface ProtocolFilePathMapper {

  Job mapInputFilePaths(final Job job, final FileMapper fileMapper) throws BindingException;
  
  Job mapOutputFilePaths(final Job job, final FileMapper fileMapper) throws BindingException;
  
}
