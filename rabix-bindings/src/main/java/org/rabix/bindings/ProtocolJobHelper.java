package org.rabix.bindings;

import org.rabix.bindings.model.Job;

public interface ProtocolJobHelper {

  boolean isSelfExecutable(Job job) throws BindingException;
  
}
