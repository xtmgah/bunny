package org.rabix.engine.service.scatter.strategy;

import org.rabix.bindings.model.ScatterMethod;
import org.rabix.engine.service.scatter.strategy.impl.ScatterCartesianStrategyHandler;
import org.rabix.engine.service.scatter.strategy.impl.ScatterZipStrategyHandler;

import com.google.inject.Inject;

public class ScatterStrategyHandlerFactory {

  private ScatterZipStrategyHandler scatterZipStrategyHandler;
  private ScatterCartesianStrategyHandler scatterCartesianStrategyHandler;
  
  @Inject
  public ScatterStrategyHandlerFactory(ScatterZipStrategyHandler scatterZipStrategyHandler, ScatterCartesianStrategyHandler scatterCartesianStrategyHandler) {
    this.scatterZipStrategyHandler = scatterZipStrategyHandler;
    this.scatterCartesianStrategyHandler = scatterCartesianStrategyHandler;
  }
  
  public ScatterStrategyHandler create(ScatterMethod scatterMethod) {
    switch (scatterMethod) {
    case dotproduct:
      return scatterZipStrategyHandler;
    case flat_crossproduct:
    case nested_crossproduct:
      return scatterCartesianStrategyHandler;
    default:
      break;
    }
    return null;
  }
  
}
