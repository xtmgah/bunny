package org.rabix.executor.service;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.Job;

public interface JobFitter {

  boolean tryToFit(Job job) throws BindingException;

  void free(Job job) throws BindingException;
  
}
