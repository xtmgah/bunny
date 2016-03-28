package org.rabix.bindings;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;

/**
 * Translates specific protocol to generic DAG format 
 */
public interface ProtocolTranslator {

  DAGNode translateToDAG(Job job) throws BindingException;
  
}
