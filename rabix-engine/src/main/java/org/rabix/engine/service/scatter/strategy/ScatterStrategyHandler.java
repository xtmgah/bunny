package org.rabix.engine.service.scatter.strategy;

import java.util.LinkedList;
import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.engine.model.DAGNodeRecord.DAGNodeGraph;
import org.rabix.engine.model.scatter.ScatterStrategy;
import org.rabix.engine.service.scatter.RowMapping;

public interface ScatterStrategyHandler {

  ScatterStrategy initialize(DAGNodeGraph dagNode);
  
  boolean isBlocking();
  
  List<RowMapping> enabled(ScatterStrategy strategy) throws BindingException;

  int enabledCount(ScatterStrategy strategy);

  void commit(ScatterStrategy strategy, List<RowMapping> mappings);

  LinkedList<Object> values(ScatterStrategy strategy, String jobId, String portId, String contextId);

  void enable(ScatterStrategy strategy, String port, Object value, Integer position);

}
