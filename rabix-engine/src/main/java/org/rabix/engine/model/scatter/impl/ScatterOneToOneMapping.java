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
  private Map<String, LinkedList<Boolean>> indexes;

  private LinkedList<Combination> combinations;

  public ScatterOneToOneMapping(DAGNode dagNode) {
    values = new HashMap<>();
    indexes = new HashMap<>();
    combinations = new LinkedList<>();

    initialize(dagNode);
  }
  
  public static void main(String[] args) {
    ScatterOneToOneMapping oneToOne = new ScatterOneToOneMapping(null);
    
    oneToOne.enable("A", 20, 2);
    System.out.println(oneToOne.getEnabledRows());
    oneToOne.enable("B", 40, 2);
    System.out.println(oneToOne.getEnabledRows());
  }
  
  public void initialize(DAGNode dagNode) {
    for(DAGLinkPort port : dagNode.getInputPorts()) {
      if (port.isScatter()) {
        values.put(port.getId(), new LinkedList<Object>());
        indexes.put(port.getId(), new LinkedList<Boolean>());
      }
    }
  }
  
  public void enable(String port, Object value, Integer position) {
    List<Object> valueList = values.get(port);
    if (position != null) {
      expand(valueList, position);
      valueList.set(position - 1, value);
    } else {
      valueList.add(value);
    }
    
    List<Boolean> indexList = indexes.get(port); 
    if (position != null) {
      expand(indexList, position);
      indexList.set(position - 1, true);
    } else {
      indexList.add(true);
    }
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
  public List<RowMapping> getEnabledRows() {
    List<RowMapping> result = new LinkedList<>();

    List<String> ports = new LinkedList<>();
    LinkedList<LinkedList<Boolean>> indexLists = new LinkedList<>();

    for (Entry<String, LinkedList<Boolean>> entry : indexes.entrySet()) {
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
        for (int index = 0; index < combinations.size(); index++) {
          Combination c = combinations.get(index);

          if (c.position == i) {
            combination = c;
            break;
          }
        }
        if (combination == null) {
          combination = new Combination(i + 1, false);
          combinations.add(combination);
        }

        if (!combination.enabled) {
          List<PortMapping> portMappings = new LinkedList<>();

          for (String portId : ports) {
            Object value = values.get(portId).get(i);
            portMappings.add(new PortMapping(portId, value));
          }
          result.add(new RowMapping(combination.position, portMappings));
        }
      }
    }
    return result;
  }

  @Override
  public void commit(List<RowMapping> mappings) {
    for (RowMapping mapping : mappings) {
      for (Combination combination : combinations) {
        if (combination.position == mapping.getIndex()) {
          combination.enabled = true;
          break;
        }
      }
    }
  }
  
  @Override
  public int getNumberOfRows() {
    return combinations.size();
  }

  private class Combination {
    int position;
    boolean enabled;

    public Combination(int position, boolean enabled) {
      this.position = position;
      this.enabled = enabled;
    }
  }

}
