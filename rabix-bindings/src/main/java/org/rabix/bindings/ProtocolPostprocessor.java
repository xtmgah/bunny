package org.rabix.bindings;

import java.io.File;

import org.rabix.bindings.model.Job;

public interface ProtocolPostprocessor {

  boolean isSuccessful(Job job, int statusCode) throws BindingException;
  
  Job postprocess(Job job, File workingDir) throws BindingException;
  
}
