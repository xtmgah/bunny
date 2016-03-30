package org.rabix.bindings;

import org.rabix.bindings.model.Job;

public interface ProtocolJobHelper {

  void validate(Job job) throws BindingException;
  
  boolean isSelfExecutable(Job job) throws BindingException;
  
}
