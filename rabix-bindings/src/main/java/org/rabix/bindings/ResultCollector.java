package org.rabix.bindings;

import java.io.File;

import org.rabix.bindings.model.Job;

public interface ResultCollector {

  boolean isSuccessful(Job job, int statusCode) throws BindingException;
  
  Job populateOutputs(Job job, File workingDir) throws BindingException;
  
}
