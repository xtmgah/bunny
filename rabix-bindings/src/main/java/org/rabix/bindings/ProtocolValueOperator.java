package org.rabix.bindings;

import java.util.Set;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.FileValue;

public interface ProtocolValueOperator {

  Set<FileValue> getInputFiles(Job job) throws BindingException;
  
  Set<FileValue> getOutputFiles(Job job) throws BindingException;
  
}
