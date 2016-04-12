package org.rabix.bindings;

import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.Job;

public interface ProtocolAppProcessor {

  void validate(Job job) throws BindingException;
  
  boolean isSelfExecutable(Job job) throws BindingException;

  String loadApp(String uri) throws BindingException;
  
  Application loadAppObject(String app) throws BindingException;

}
