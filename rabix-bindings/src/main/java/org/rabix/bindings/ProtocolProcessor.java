package org.rabix.bindings;

import java.io.File;

import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.Executable;

public interface ProtocolProcessor {

  Executable preprocess(final Executable executable, final File workingDir) throws BindingException;
  
  Executable mapInputFilePaths(final Executable executable, final FileMapper fileMapper) throws BindingException;
  
  Executable mapOutputFilePaths(final Executable executable, final FileMapper fileMapper) throws BindingException;
  
}
