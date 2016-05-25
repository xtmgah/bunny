package org.rabix.engine.service.scatter;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.service.scatter.strategy.ScatterCartesianStrategyHandler;
import org.rabix.engine.service.scatter.strategy.ScatterStrategyHandler;
import org.rabix.engine.service.scatter.strategy.ScatterZipStrategyHandler;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class ScatterStrategyFactory {

  private final VariableRecordService variableRecordService;

  @Inject
  public ScatterStrategyFactory(VariableRecordService variableRecordService) {
    this.variableRecordService = variableRecordService;
  }
  
  public ScatterStrategyHandler create(DAGNode dagNode) throws BindingException {
    Preconditions.checkNotNull(dagNode);
    
    switch (dagNode.getScatterMethod()) {
    case dotproduct:
      return new ScatterZipStrategyHandler(dagNode, variableRecordService);
    case flat_crossproduct:
    case nested_crossproduct:
      return new ScatterCartesianStrategyHandler(dagNode, variableRecordService);
    default:
      throw new BindingException("Scatter method " + dagNode.getScatterMethod() + " is not supported.");
    }
  }
  
}
