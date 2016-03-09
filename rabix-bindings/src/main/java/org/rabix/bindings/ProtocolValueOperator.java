package org.rabix.bindings;

import java.util.Set;

import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.FileValue;

public interface ProtocolValueOperator {

  Set<FileValue> getInputFiles(Executable executable) throws BindingException;
  
  Set<FileValue> getOutputFiles(Executable executable) throws BindingException;
  
  Object getInputValueById(Object inputs, String id);
  
  Object getOutputValueById(Object outputs, String id);
  
  Object addToInputs(Object inputs, String id, Object value) throws BindingException;
  
  Object addToOutputs(Object outputs, String id, Object value) throws BindingException;

}
