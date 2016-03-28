package org.rabix.bindings;

import java.util.Map;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;

/**
 * Translates specific protocol to generic DAG format 
 */
public interface ProtocolTranslator {

  DAGNode translateToDAG(Job job) throws BindingException;
  
  /**
   * Translates to DAG format
   */
  DAGNode translateToDAGFromPayload(String payload) throws BindingException;
  
  /**
   * Get inputs from the pay-load
   */
  Map<String, Object> translateInputsFromPayload(String payload);
  
  /**
   * Translates to DAG format
   */
  DAGNode translateToDAG(String app, String inputs) throws BindingException;
  
  
  /**
   * Get inputs from the inputs
   */
  Map<String, Object> translateInputs(String inputs) throws BindingException;
}
