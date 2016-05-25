package org.rabix.engine.service.scatter.strategy;

import java.util.LinkedList;
import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.engine.service.scatter.RowMapping;

public interface ScatterStrategyHandler {

  void enable(String port, Object value, Integer position);

  void commit(List<RowMapping> mappings);
  
  int enabledCount();
  
  boolean isBlocking();
  
  List<RowMapping> enabled() throws BindingException;
  
  LinkedList<Object> values(String jobId, String portId, String contextId);
  
}
