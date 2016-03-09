package org.rabix.executor.handler;

import org.rabix.bindings.model.Executable;

public interface ExecutableHandlerFactory {

  ExecutableHandler createHandler(Executable executable);
  
}
