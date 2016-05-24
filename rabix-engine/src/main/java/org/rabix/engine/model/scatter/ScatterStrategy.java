package org.rabix.engine.model.scatter;

import java.util.LinkedList;
import java.util.List;

import org.rabix.bindings.BindingException;

public interface ScatterStrategy {

  void enable(String port, Object value, Integer position);

  void commit(List<RowMapping> mappings);
  
  int enabledCount();
  
  boolean isBlocking();
  
  List<RowMapping> enabled() throws BindingException;
  
  LinkedList<Object> values(String jobId, String portId, String contextId);
  
}
