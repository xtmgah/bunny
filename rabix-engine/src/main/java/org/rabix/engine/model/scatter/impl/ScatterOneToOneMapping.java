package org.rabix.engine.model.scatter.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.model.scatter.PortMapping;
import org.rabix.engine.model.scatter.RowMapping;
import org.rabix.engine.model.scatter.ScatterMapping;

public class ScatterOneToOneMapping implements ScatterMapping {

  private Map<String, LinkedList<Object>> values;
  private Map<String, LinkedList<Integer>> indexes;

  private LinkedList<Combination> combinations;

  public ScatterOneToOneMapping(DAGNode dagNode) {
    values = new HashMap<>();
    indexes = new HashMap<>();
    combinations = new LinkedList<>();

    initialize(dagNode);
  }
  
  public void initialize(DAGNode dagNode) {
    for(DAGLinkPort port : dagNode.getInputPorts()) {
      if (port.isScatter()) {
        values.put(port.getId(), new LinkedList<Object>());
        indexes.put(port.getId(), new LinkedList<Integer>());
      }
    }
  }
  
  public void enable(String port, Object value) {
    values.get(port).add(value);
    indexes.get(port).add(indexes.get(port).size());
  }

  @Override
  public List<RowMapping> getEnabledRows() {
    List<RowMapping> result = new LinkedList<>();

    List<String> ports = new LinkedList<>();
    LinkedList<LinkedList<Integer>> indexLists = new LinkedList<>();

    for (Entry<String, LinkedList<Integer>> entry : indexes.entrySet()) {
      ports.add(entry.getKey());
      indexLists.add(entry.getValue());
    }

    for (int i = 0; i < indexLists.get(0).size(); i++) {
      boolean exists = true;
      for (LinkedList<Integer> indexList : indexLists) {
        if (indexList.get(i) == null) {
          exists = false;
          break;
        }
      }
      if (!exists) {
        break;
      } else {
        Combination combination = null;
        for (int index = 0; index < combinations.size(); index++) {
          Combination c = combinations.get(index);

          if (c.row == i) {
            combination = c;
            break;
          }
        }
        if (combination == null) {
          combination = new Combination(combinations.size(), false);
          combinations.add(combination);
        }

        if (!combination.enabled) {
          List<PortMapping> portMappings = new LinkedList<>();

          for (String portId : ports) {
            Object value = values.get(portId).get(i);
            portMappings.add(new PortMapping(portId, value));
          }
          result.add(new RowMapping(combination.row, portMappings));
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

  private class Combination {
    int row;
    boolean enabled;

    public Combination(int row, boolean enabled) {
      this.row = row;
      this.enabled = enabled;
    }
  }

}
