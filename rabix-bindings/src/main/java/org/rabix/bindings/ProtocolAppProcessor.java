package org.rabix.bindings;

import org.rabix.bindings.model.Job;

public interface ProtocolAppProcessor {

  void validate(Job job) throws BindingException;
  
  boolean isSelfExecutable(Job job) throws BindingException;
  
  Object getAppObject(String app) throws BindingException;
}
