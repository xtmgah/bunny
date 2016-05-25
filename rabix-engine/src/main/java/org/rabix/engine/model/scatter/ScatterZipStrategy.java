package org.rabix.engine.model.scatter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.rabix.bindings.model.ScatterMethod;

public class ScatterZipStrategy implements ScatterStrategy {

  private ScatterMethod scatterMethod;
  private LinkedList<Combination> combinations = new LinkedList<>();
  
  private Map<String, LinkedList<Object>> values = new HashMap<>();
  private Map<String, LinkedList<Boolean>> indexes = new HashMap<>();

  public ScatterZipStrategy() {
    this.scatterMethod = ScatterMethod.dotproduct;
  }
  
  public ScatterMethod getScatterMethod() {
    return scatterMethod;
  }

  public void setScatterMethod(ScatterMethod scatterMethod) {
    this.scatterMethod = scatterMethod;
  }

  public LinkedList<Combination> getCombinations() {
    return combinations;
  }

  public void setCombinations(LinkedList<Combination> combinations) {
    this.combinations = combinations;
  }

  public Map<String, LinkedList<Object>> getValues() {
    return values;
  }

  public void setValues(Map<String, LinkedList<Object>> values) {
    this.values = values;
  }

  public Map<String, LinkedList<Boolean>> getIndexes() {
    return indexes;
  }

  public void setIndexes(Map<String, LinkedList<Boolean>> indexes) {
    this.indexes = indexes;
  }

  
  @Override
  public String toString() {
    return "ScatterZipStrategyModel [scatterMethod=" + scatterMethod + ", combinations=" + combinations + ", values=" + values + ", indexes=" + indexes + "]";
  }

  public static class Combination {
    private int position;
    private boolean enabled;

    public Combination(int position, boolean enabled) {
      this.position = position;
      this.enabled = enabled;
    }

    public int getPosition() {
      return position;
    }

    public void setPosition(int position) {
      this.position = position;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    @Override
    public String toString() {
      return "Combination [position=" + position + ", enabled=" + enabled + "]";
    }
    
  }
  
}
