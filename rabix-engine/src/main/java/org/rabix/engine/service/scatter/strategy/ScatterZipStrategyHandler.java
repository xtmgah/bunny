package org.rabix.engine.service.scatter.strategy;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.rabix.bindings.model.ScatterMethod;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.model.scatter.ScatterZipStrategy;
import org.rabix.engine.model.scatter.ScatterZipStrategy.Combination;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.service.scatter.PortMapping;
import org.rabix.engine.service.scatter.RowMapping;

import com.google.common.base.Preconditions;

public class ScatterZipStrategyHandler implements ScatterStrategyHandler {

  private final ScatterZipStrategy strategy;
  private final VariableRecordService variableRecordService;
  
  public ScatterZipStrategyHandler(DAGNode dagNode, VariableRecordService variableRecordService) {
    this.strategy = new ScatterZipStrategy();
    this.variableRecordService = variableRecordService;
    initialize(dagNode);
  }
  
  public void initialize(DAGNode dagNode) {
    for(DAGLinkPort port : dagNode.getInputPorts()) {
      if (port.isScatter()) {
        strategy.getValues().put(port.getId(), new LinkedList<Object>());
        strategy.getIndexes().put(port.getId(), new LinkedList<Boolean>());
      }
    }
  }
  
  public void enable(String port, Object value, Integer position) {
    Preconditions.checkNotNull(port);
    Preconditions.checkNotNull(position);
    
    List<Object> valueList = strategy.getValues().get(port);
    List<Boolean> indexList = strategy.getIndexes().get(port);

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
  public List<RowMapping> enabled() {
    List<RowMapping> result = new LinkedList<>();

    List<String> ports = new LinkedList<>();
    LinkedList<LinkedList<Boolean>> indexLists = new LinkedList<>();

    for (Entry<String, LinkedList<Boolean>> entry : strategy.getIndexes().entrySet()) {
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
        for (int index = 0; index < strategy.getCombinations().size(); index++) {
          Combination c = strategy.getCombinations().get(index);

          if (c.getPosition() == i + 1) {
            combination = c;
            break;
          }
        }
        if (combination == null) {
          combination = new Combination(i + 1, false);
          strategy.getCombinations().add(combination);
        }

        if (!combination.isEnabled()) {
          List<PortMapping> portMappings = new LinkedList<>();

          for (String portId : ports) {
            Object value = strategy.getValues().get(portId).get(i);
            portMappings.add(new PortMapping(portId, value));
          }
          result.add(new RowMapping(combination.getPosition(), portMappings));
        }
      }
    }
    return result;
  }

  @Override
  public void commit(List<RowMapping> mappings) {
    for (RowMapping mapping : mappings) {
      for (Combination combination : strategy.getCombinations()) {
        if (combination.getPosition() == mapping.getIndex()) {
          combination.setEnabled(true);
          break;
        }
      }
    }
  }
  
  @Override
  public LinkedList<Object> values(String jobId, String portId, String contextId) {
    Collections.sort(strategy.getCombinations(), new Comparator<Combination>() {
      @Override
      public int compare(Combination o1, Combination o2) {
        return o1.getPosition() - o2.getPosition();
      }
    });
    
    LinkedList<Object> result = new LinkedList<>();
    for (Combination combination : strategy.getCombinations()) {
      String scatteredJobId = InternalSchemaHelper.scatterId(jobId, combination.getPosition());
      VariableRecord variableRecord = variableRecordService.find(scatteredJobId, portId, LinkPortType.OUTPUT, contextId);
      result.addLast(variableRecord.getValue());
    }
    return result;
  }

  
  @Override
  public int enabledCount() {
    return strategy.getCombinations().size();
  }

  @Override
  public boolean isBlocking() {
    return ScatterMethod.isBlocking(strategy.getScatterMethod());
  }

}
