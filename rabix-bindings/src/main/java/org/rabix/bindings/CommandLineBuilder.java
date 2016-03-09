package org.rabix.bindings;

import java.util.List;

import org.rabix.bindings.model.Executable;

public interface CommandLineBuilder {

  String buildCommandLine(Executable executable) throws BindingException;
  
  List<Object> buildCommandLineParts(Executable executable) throws BindingException;
  
}
