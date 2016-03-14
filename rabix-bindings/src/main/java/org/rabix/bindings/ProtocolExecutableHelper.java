package org.rabix.bindings;

import org.rabix.bindings.model.Executable;

public interface ProtocolExecutableHelper {

  Object getApp(Executable executable) throws BindingException;
  
  boolean isSelfExecutable(Executable executable) throws BindingException;
  
}
