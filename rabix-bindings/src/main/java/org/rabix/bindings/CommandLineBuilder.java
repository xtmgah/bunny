package org.rabix.bindings;

import java.util.List;

import org.rabix.bindings.model.Job;

public interface CommandLineBuilder {

  String buildCommandLine(Job job) throws BindingException;
  
  List<Object> buildCommandLineParts(Job job) throws BindingException;
  
}
