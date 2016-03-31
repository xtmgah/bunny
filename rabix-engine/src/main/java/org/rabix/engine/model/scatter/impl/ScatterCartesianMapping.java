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
  private Map<String, LinkedList<Integer>> indexes;

  public ScatterCartesianMapping(DAGNode dagNode) {
    this.values = new HashMap<>();
    this.indexes = new HashMap<>();
    this.combinations = new LinkedList<>();

    initialize(dagNode);
  }

  private void initialize(DAGNode dagNode) {
    for (DAGLinkPort port : dagNode.getInputPorts()) {
      if (port.isScatter()) {
        values.put(port.getId(), new LinkedList<Object>());
        indexes.put(port.getId(), new LinkedList<Integer>());
      }
    }
  }

  public void enable(String port, Object value, Integer position) {
    this.values.get(port).addLast(value);
    this.indexes.get(port).addLast(indexes.get(port).size());
  }

  public List<RowMapping> getEnabledRows() throws BindingException {
    List<RowMapping> result = new LinkedList<>();

    List<String> ports = new LinkedList<>();
    LinkedList<LinkedList<Integer>> lists = new LinkedList<>();

    for (Entry<String, LinkedList<Integer>> entry : indexes.entrySet()) {
      ports.add(entry.getKey());
      lists.add(entry.getValue());
    }

    LinkedList<LinkedList<Integer>> product = cartesianProduct(lists);

    for (LinkedList<Integer> list : product) {
      Combination combination = null;
      for (int index = 0; index < combinations.size(); index++) {
        Combination c = combinations.get(index);

        if (c.combination.equals(list)) {
          combination = c;
          break;
        }
      }
      if (combination == null) {
        combination = new Combination(combinations.size(), false, list);
        combinations.add(combination);
      }

      if (!combination.enabled) {
        List<PortMapping> portMappings = new LinkedList<>();

        for (int subindex = 0; subindex < list.size(); subindex++) {
          String portId = ports.get(subindex);
          Object value = values.get(portId).get(list.get(subindex));
          portMappings.add(new PortMapping(portId, value));
        }

        result.add(new RowMapping(combination.index, portMappings));
      }
    }
    return result;
  }

  public void commit(List<RowMapping> mappings) {
    for (RowMapping mapping : mappings) {
      Combination combination = combinations.get(mapping.getIndex());
      combination.enabled = true;
    }
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
  public int getNumberOfRows() {
    return combinations.size();
  }
  
  private class Combination {
    int index;
    boolean enabled;
    LinkedList<Integer> combination;

    public Combination(int index, boolean enabled, LinkedList<Integer> combination) {
      this.index = index;
      this.enabled = enabled;
      this.combination = combination;
    }
  }

}
