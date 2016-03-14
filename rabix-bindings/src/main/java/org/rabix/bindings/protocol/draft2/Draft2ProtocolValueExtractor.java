package org.rabix.bindings.protocol.draft2;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolValueOperator;
import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.helper.Draft2ProtocolExecutableHelper;
import org.rabix.bindings.protocol.draft2.processor.Draft2PortProcessorException;
import org.rabix.bindings.protocol.draft2.processor.callback.Draft2PortProcessorHelper;
import org.rabix.common.helper.CloneHelper;

public class Draft2ProtocolValueExtractor implements ProtocolValueOperator {

  @SuppressWarnings("unchecked")
  public Set<FileValue> getInputFiles(Executable executable) throws BindingException {
    Draft2Job draft2Job = new Draft2ProtocolExecutableHelper().getJob(executable);
    try {
      return new Draft2PortProcessorHelper(draft2Job).flattenInputFiles(executable.getInputs(Map.class));
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<FileValue> getOutputFiles(Executable executable) throws BindingException {
    Draft2Job draft2Job = new Draft2ProtocolExecutableHelper().getJob(executable);
    try {
      return new Draft2PortProcessorHelper(draft2Job).flattenOutputFiles(executable.getInputs(Map.class));
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object getInputValueById(Object inputs, String id) {
    if (inputs == null) {
      return null;
    }
    return ((Map<String, Object>)inputs).get(id);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object getOutputValueById(Object outputs, String id) {
    if (outputs == null) {
      return null;
    }
    return ((Map<String, Object>) outputs).get(id);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object addToInputs(Object inputs, String id, Object value) throws BindingException {
    if (inputs != null && !(inputs instanceof Map<?, ?>)) {
      throw new BindingException("Inputs are not according to CWL Draft 2 specification.");
    }
    try {
      Map<String, Object> newInputs = inputs == null ? new HashMap<String, Object>() : (Map<String, Object>) CloneHelper.deepCopy(inputs);
      newInputs.put(id, value);
      return newInputs;
    } catch (Exception e) {
      throw new BindingException("Failed to form CWL Draft 2 inputs");
    }
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public Object addToOutputs(Object outputs, String id, Object value) throws BindingException {
    if (outputs != null && !(outputs instanceof Map<?, ?>)) {
      throw new BindingException("Outputs are not according to CWL Draft 2 specification.");
    }
    try {
      Map<String, Object> newOutputs = outputs == null ? new HashMap<String, Object>() : (Map<String, Object>) CloneHelper.deepCopy(outputs);
      newOutputs.put(id, value);
      return newOutputs;
    } catch (Exception e) {
      throw new BindingException("Failed to form CWL Draft 2 inputs");
    }
  }

}
