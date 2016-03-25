package org.rabix.bindings;

import org.rabix.bindings.model.Job;

public interface ProtocolJobHelper {

  Object getJob(Job job) throws BindingException;
  
  Object getApp(Job job) throws BindingException;
  
  boolean isSelfExecutable(Job job) throws BindingException;
  
}
