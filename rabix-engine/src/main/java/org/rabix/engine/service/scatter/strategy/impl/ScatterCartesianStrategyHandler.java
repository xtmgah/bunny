package org.rabix.engine.service.scatter.strategy.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.ScatterMethod;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.model.scatter.ScatterStrategy;
import org.rabix.engine.model.scatter.impl.ScatterCartesianStrategy;
import org.rabix.engine.model.scatter.impl.ScatterCartesianStrategy.Combination;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.service.scatter.PortMapping;
import org.rabix.engine.service.scatter.RowMapping;
import org.rabix.engine.service.scatter.strategy.ScatterStrategyHandler;

import com.google.inject.Inject;

public class ScatterCartesianStrategyHandler implements ScatterStrategyHandler {

  private final VariableRecordService variableRecordService;

  @Inject
  public ScatterCartesianStrategyHandler(VariableRecordService variableRecordService) {
    this.variableRecordService = variableRecordService;
  }

  @Override
  public ScatterStrategy initialize(DAGNode dagNode) {
    ScatterCartesianStrategy strategy = new ScatterCartesianStrategy(dagNode.getScatterMethod());
    for (DAGLinkPort port : dagNode.getInputPorts()) {
      if (port.isScatter()) {
        strategy.getValues().put(port.getId(), new LinkedList<Object>());
        strategy.getPositions().put(port.getId(), new LinkedList<Integer>());
      }
    }
    return strategy;
  }

  @Override
  public void enable(ScatterStrategy strategy, String port, Object value, Integer position) {
    ScatterCartesianStrategy scatterCartesianStrategy = (ScatterCartesianStrategy) strategy;
    LinkedList<Integer> positionList = scatterCartesianStrategy.getPositions().get(port);
    positionList = expand(positionList, position);
    positionList.set(position - 1, position);
    scatterCartesianStrategy.getPositions().put(port, positionList);

    LinkedList<Object> valueList = scatterCartesianStrategy.getValues().get(port);
    valueList = expand(valueList, position);
    valueList.set(position - 1, value);
    scatterCartesianStrategy.getValues().put(port, valueList);
  }

  @Override
  public LinkedList<Object> values(ScatterStrategy strategy, String jobId, String portId, String contextId) {
    ScatterCartesianStrategy scatterCartesianStrategy = (ScatterCartesianStrategy) strategy;
    Collections.sort(scatterCartesianStrategy.getCombinations(), new Comparator<Combination>() {
      @Override
      public int compare(Combination o1, Combination o2) {
        return o1.getIndexes().toString().compareTo(o2.getIndexes().toString());
      }
    });

    if (scatterCartesianStrategy.getScatterMethod().equals(ScatterMethod.flat_crossproduct)) {
      LinkedList<Object> result = new LinkedList<>();
      for (Combination combination : scatterCartesianStrategy.getCombinations()) {
        String scatteredJobId = InternalSchemaHelper.scatterId(jobId, combination.getPosition());
        VariableRecord variableRecord = variableRecordService.find(scatteredJobId, portId, LinkPortType.OUTPUT, contextId);
        result.addLast(variableRecordService.transformValue(variableRecord));
      }
      return result;
    }
    if (scatterCartesianStrategy.getScatterMethod().equals(ScatterMethod.nested_crossproduct)) {
      LinkedList<Object> result = new LinkedList<>();
      
      int position = 1;
      LinkedList<Object> subresult = new LinkedList<>();
      for (Combination combination : scatterCartesianStrategy.getCombinations()) {
        if (combination.getIndexes().get(0) != position) {
          result.addLast(subresult);
          subresult = new LinkedList<>();
          position++;
        }
        String scatteredJobId = InternalSchemaHelper.scatterId(jobId, combination.getPosition());
        VariableRecord variableRecord = variableRecordService.find(scatteredJobId, portId, LinkPortType.OUTPUT, contextId);
        subresult.addLast(variableRecordService.transformValue(variableRecord));
      }
      result.addLast(subresult);
      return result;
    }
    return null;
  }
  
  private <T> LinkedList<T> expand(LinkedList<T> list, Integer position) {
    if (list == null) {
      list = new LinkedList<>();
    }
    int initialSize = list.size();
    if (initialSize >= position) {
      return list;
    }
    for (int i = 0; i < position - initialSize; i++) {
      list.add(null);
    }
    return list;
  }

  private LinkedList<LinkedList<Integer>> cartesianProduct(LinkedList<LinkedList<Integer>> lists) throws BindingException {
    if (lists.size() < 2) {
      throw new BindingException("Can't have a product of fewer than two lists (got " + lists.size() + ")");
    }
    return cartesianProduct(0, lists);
  }

  private LinkedList<LinkedList<Integer>> cartesianProduct(int index, List<LinkedList<Integer>> lists) {
    LinkedList<LinkedList<Integer>> result = new LinkedList<LinkedList<Integer>>();
    if (index == lists.size()) {
      result.add(new LinkedList<Integer>());
    } else {
      for (Integer obj : lists.get(index)) {
        for (LinkedList<Integer> list : cartesianProduct(index + 1, lists)) {
          list.addFirst(obj);
          result.add(list);
        }
      }
    }
    return result;
  }

  @Override
  public void commit(ScatterStrategy strategy, List<RowMapping> mappings) {
    ScatterCartesianStrategy scatterCartesianStrategy = (ScatterCartesianStrategy) strategy;
    for (RowMapping mapping : mappings) {
      for (Combination combination : scatterCartesianStrategy.getCombinations()) {
        if (combination.getPosition() == mapping.getIndex()) {
          combination.setEnabled(true);
        }
      }
    }
  }

  @Override
  public int enabledCount(ScatterStrategy strategy) {
    ScatterCartesianStrategy scatterCartesianStrategy = (ScatterCartesianStrategy) strategy;
    return scatterCartesianStrategy.getCombinations().size();
  }

  @Override
  public List<RowMapping> enabled(ScatterStrategy strategy) throws BindingException {
    ScatterCartesianStrategy scatterCartesianStrategy = (ScatterCartesianStrategy) strategy;
    List<RowMapping> result = new LinkedList<>();
    LinkedList<LinkedList<Integer>> mapping = new LinkedList<>();
    for (Entry<String, LinkedList<Integer>> positionEntry : scatterCartesianStrategy.getPositions().entrySet()) {
      mapping.add(positionEntry.getValue());
    }
    LinkedList<LinkedList<Integer>> newMapping = cartesianProduct(mapping);

    for (int i = 0; i < newMapping.size(); i++) {
      LinkedList<Integer> indexes = newMapping.get(i);
      if (!hasNull(indexes)) {
        Combination combination = getCombination(scatterCartesianStrategy, indexes);
        if (combination == null) {
          combination = new Combination(scatterCartesianStrategy.getCombinations().size() + 1, false, indexes);
          scatterCartesianStrategy.getCombinations().add(combination);
        }
        if (!combination.isEnabled()) {
          List<PortMapping> portMappings = new LinkedList<>();

          int positionIndex = 1;
          for (Entry<String, LinkedList<Object>> valueEntry : scatterCartesianStrategy.getValues().entrySet()) {
            String port = valueEntry.getKey();
            int position = combination.getIndexes().get(positionIndex - 1);
            Object value = valueEntry.getValue().get(position - 1);
            portMappings.add(new PortMapping(port, value));
            positionIndex++;
          }
          result.add(new RowMapping(combination.getPosition(), portMappings));
        }
      }
    }
    return result;
  }

  private boolean hasNull(LinkedList<Integer> list) {
    for (Integer value : list) {
      if (value == null) {
        return true;
      }
    }
    return false;
  }

  private Combination getCombination(ScatterCartesianStrategy strategy, LinkedList<Integer> indexes) {
    for (Combination combination : strategy.getCombinations()) {
      if (combination.getIndexes().toString().equals(indexes.toString())) {
        return combination;
      }
    }
    return null;
  }

  public ScatterMethod getScatterMethod(Object strategy) {
    ScatterCartesianStrategy scatterCartesianStrategy = (ScatterCartesianStrategy) strategy;
    return scatterCartesianStrategy.getScatterMethod();
  }
  
  @Override
  public boolean isBlocking() {
    return true;
  }

}
