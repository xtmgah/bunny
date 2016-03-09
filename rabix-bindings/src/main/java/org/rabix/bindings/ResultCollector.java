package org.rabix.bindings;

import java.io.File;

import org.rabix.bindings.model.Executable;

public interface ResultCollector {

  boolean isSuccessfull(Executable executable, int statusCode) throws BindingException;
  
  Executable populateOutputs(Executable executable, File workingDir) throws BindingException;
  
}
