package org.rabix.bindings;

import org.rabix.bindings.model.Executable;

public interface ProtocolExecutableHelper {

  Object getJob(Executable executable) throws BindingException;
  
  Object getApp(Executable executable) throws BindingException;
  
  boolean isSelfExecutable(Executable executable) throws BindingException;
  
}
