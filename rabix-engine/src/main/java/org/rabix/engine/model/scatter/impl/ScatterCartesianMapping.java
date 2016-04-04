package org.rabix.engine.model.scatter.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.model.scatter.PortMapping;
import org.rabix.engine.model.scatter.RowMapping;
import org.rabix.engine.model.scatter.ScatterMapping;

public class ScatterCartesianMapping implements ScatterMapping {

  private LinkedList<Combination> combinations;

  private Map<String, LinkedList<Object>> values;
  private Map<String, LinkedList<Integer>> positions;

  public ScatterCartesianMapping(DAGNode dagNode) {
    this.values = new HashMap<>();
    this.positions = new HashMap<>();
    this.combinations = new LinkedList<>();
    initialize(dagNode);
  }

  public void initialize(DAGNode dagNode) {
    for (DAGLinkPort port : dagNode.getInputPorts()) {
      if (port.isScatter()) {
        values.put(port.getId(), new LinkedList<Object>());
        positions.put(port.getId(), new LinkedList<Integer>());
      }
    }
  }

  @Override
  public void enable(String port, Object value, Integer position) {
    LinkedList<Integer> positionList = positions.get(port);
    positionList = expand(positionList, position);
    positionList.set(position - 1, position);
    positions.put(port, positionList);

    LinkedList<Object> valueList = values.get(port);
    valueList = expand(valueList, position);
    valueList.set(position - 1, value);
    values.put(port, valueList);
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

  private LinkedList<LinkedList<Integer>> cartesianProduct(LinkedList<LinkedList<Integer>> lists)
      throws BindingException {
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
  public void commit(List<RowMapping> mappings) {
    for (RowMapping mapping : mappings) {
      Combination combination = combinations.get(mapping.getIndex());
      combination.enabled = true;
    }
  }

  @Override
  public int getNumberOfRows() {
    return combinations.size();
  }

  @Override
  public List<RowMapping> getEnabledRows() throws BindingException {
    List<RowMapping> result = new LinkedList<>();
    LinkedList<LinkedList<Integer>> mapping = new LinkedList<>();
    for (Entry<String, LinkedList<Integer>> positionEntry : positions.entrySet()) {
      mapping.add(positionEntry.getValue());
    }
    LinkedList<LinkedList<Integer>> newMapping = cartesianProduct(mapping);

    for (int i = 0; i < newMapping.size(); i++) {
      LinkedList<Integer> positions = newMapping.get(i);
      if (!hasNull(positions)) {
        Combination combination = getCombination(i + 1);
        if (combination == null) {
          combination = new Combination(i + 1, false, positions);
          combinations.add(combination);
        }
        if (!combination.enabled) {
          List<PortMapping> portMappings = new LinkedList<>();

          int positionIndex = 1;
          for (Entry<String, LinkedList<Object>> valueEntry : values.entrySet()) {
            String port = valueEntry.getKey();
            int position = combination.combination.get(positionIndex - 1);
            Object value = valueEntry.getValue().get(position - 1);
            portMappings.add(new PortMapping(port, value));
            positionIndex++;
          }
          result.add(new RowMapping(combination.position, portMappings));
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

  private Combination getCombination(int position) {
    for (Combination combination : combinations) {
      if (combination.position == position) {
        return combination;
      }
    }
    return null;
  }

  private class Combination {
    int position;
    boolean enabled;
    LinkedList<Integer> combination;

    public Combination(int position, boolean enabled, LinkedList<Integer> combination) {
      this.position = position;
      this.enabled = enabled;
      this.combination = combination;
    }
  }

}
