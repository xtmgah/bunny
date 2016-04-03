package org.rabix.bindings;

import java.io.File;

import org.rabix.bindings.model.Job;

public interface ProtocolProcessor {

  Job preprocess(Job job, File workingDir) throws BindingException;

  Job postprocess(Job job, File workingDir) throws BindingException;

  boolean isSuccessful(Job job, int statusCode) throws BindingException;
  
}
