package org.rabix.engine.service.scatter.strategy.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.model.scatter.ScatterStrategy;
import org.rabix.engine.model.scatter.impl.ScatterZipStrategy;
import org.rabix.engine.model.scatter.impl.ScatterZipStrategy.Combination;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.service.scatter.PortMapping;
import org.rabix.engine.service.scatter.RowMapping;
import org.rabix.engine.service.scatter.strategy.ScatterStrategyHandler;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class ScatterZipStrategyHandler implements ScatterStrategyHandler {

  private final VariableRecordService variableRecordService;
  
  @Inject
  public ScatterZipStrategyHandler(VariableRecordService variableRecordService) {
    this.variableRecordService = variableRecordService;
  }
  
  @Override
  public ScatterStrategy initialize(DAGNode dagNode) {
    ScatterZipStrategy strategy = new ScatterZipStrategy();
    for(DAGLinkPort port : dagNode.getInputPorts()) {
      if (port.isScatter()) {
        strategy.getValues().put(port.getId(), new LinkedList<Object>());
        strategy.getIndexes().put(port.getId(), new LinkedList<Boolean>());
      }
    }
    return strategy;
  }
  
  public void enable(ScatterStrategy strategy, String port, Object value, Integer position) {
    Preconditions.checkNotNull(port);
    Preconditions.checkNotNull(position);
    
    ScatterZipStrategy scatterZipStrategy = (ScatterZipStrategy) strategy;
    
    List<Object> valueList = scatterZipStrategy.getValues().get(port);
    List<Boolean> indexList = scatterZipStrategy.getIndexes().get(port);

    if (indexList.size() < position) {
      expand(indexList, position);
      expand(valueList, position);
    }

    indexList.set(position - 1, true);
    valueList.set(position - 1, value);
  }
  
  private <T> void expand(List<T> list, Integer position) {
    int initialSize = list.size();
    if (initialSize >= position) {
      return;
    }
    for (int i = 0; i < position - initialSize; i++) {
      list.add(null);
    }
    return;
  }

  @Override
  public List<RowMapping> enabled(ScatterStrategy strategy) {
    ScatterZipStrategy scatterZipStrategy = (ScatterZipStrategy) strategy;
    
    List<RowMapping> result = new LinkedList<>();

    List<String> ports = new LinkedList<>();
    LinkedList<LinkedList<Boolean>> indexLists = new LinkedList<>();

    for (Entry<String, LinkedList<Boolean>> entry : scatterZipStrategy.getIndexes().entrySet()) {
      ports.add(entry.getKey());
      indexLists.add(entry.getValue());
    }

    List<Boolean> first = indexLists.get(0);
    for (int i = 0; i < first.size(); i++) {
      if (first.get(i) == null) {
        continue;
      }
      boolean exists = true;
      for (LinkedList<Boolean> indexList : indexLists) {
        if (indexList.size() <= i || indexList.get(i) == null) {
          exists = false;
          break;
        }
      }
      if (!exists) {
        break;
      } else {
        Combination combination = null;
        for (int index = 0; index < scatterZipStrategy.getCombinations().size(); index++) {
          Combination c = scatterZipStrategy.getCombinations().get(index);

          if (c.getPosition() == i + 1) {
            combination = c;
            break;
          }
        }
        if (combination == null) {
          combination = new Combination(i + 1, false);
          scatterZipStrategy.getCombinations().add(combination);
        }

        if (!combination.isEnabled()) {
          List<PortMapping> portMappings = new LinkedList<>();

          for (String portId : ports) {
            Object value = scatterZipStrategy.getValues().get(portId).get(i);
            portMappings.add(new PortMapping(portId, value));
          }
          result.add(new RowMapping(combination.getPosition(), portMappings));
        }
      }
    }
    return result;
  }

  @Override
  public void commit(ScatterStrategy strategy, List<RowMapping> mappings) {
    ScatterZipStrategy scatterZipStrategy = (ScatterZipStrategy) strategy;
    for (RowMapping mapping : mappings) {
      for (Combination combination : scatterZipStrategy.getCombinations()) {
        if (combination.getPosition() == mapping.getIndex()) {
          combination.setEnabled(true);
          break;
        }
      }
    }
  }
  
  @Override
  public LinkedList<Object> values(ScatterStrategy strategy, String jobId, String portId, String contextId) {
    ScatterZipStrategy scatterZipStrategy = (ScatterZipStrategy) strategy;
    
    Collections.sort(scatterZipStrategy.getCombinations(), new Comparator<Combination>() {
      @Override
      public int compare(Combination o1, Combination o2) {
        return o1.getPosition() - o2.getPosition();
      }
    });
    
    LinkedList<Object> result = new LinkedList<>();
    for (Combination combination : scatterZipStrategy.getCombinations()) {
      String scatteredJobId = InternalSchemaHelper.scatterId(jobId, combination.getPosition());
      VariableRecord variableRecord = variableRecordService.find(scatteredJobId, portId, LinkPortType.OUTPUT, contextId);
      result.addLast(variableRecordService.transformValue(variableRecord));
    }
    return result;
  }

  
  @Override
  public int enabledCount(ScatterStrategy strategy) {
    ScatterZipStrategy scatterZipStrategy = (ScatterZipStrategy) strategy;
    return scatterZipStrategy.getCombinations().size();
  }

  @Override
  public boolean isBlocking() {
    return false;
  }

}
