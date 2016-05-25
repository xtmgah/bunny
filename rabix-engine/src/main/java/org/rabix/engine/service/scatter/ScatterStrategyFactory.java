package org.rabix.engine.service.scatter;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.model.scatter.ScatterStrategy;
import org.rabix.engine.model.scatter.strategy.ScatterCartesianStrategy;
import org.rabix.engine.model.scatter.strategy.ScatterZipStrategy;
import org.rabix.engine.service.VariableRecordService;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class ScatterStrategyFactory {

  private final VariableRecordService variableRecordService;

  @Inject
  public ScatterStrategyFactory(VariableRecordService variableRecordService) {
    this.variableRecordService = variableRecordService;
  }
  
  public ScatterStrategy create(DAGNode dagNode) throws BindingException {
    Preconditions.checkNotNull(dagNode);
    
    switch (dagNode.getScatterMethod()) {
    case dotproduct:
      return new ScatterZipStrategy(dagNode, variableRecordService);
    case flat_crossproduct:
    case nested_crossproduct:
      return new ScatterCartesianStrategy(dagNode, variableRecordService);
    default:
      throw new BindingException("Scatter method " + dagNode.getScatterMethod() + " is not supported.");
    }
  }
  
}
