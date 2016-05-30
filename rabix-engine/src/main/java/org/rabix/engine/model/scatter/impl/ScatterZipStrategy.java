package org.rabix.engine.model.scatter.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.rabix.bindings.model.ScatterMethod;
import org.rabix.engine.model.scatter.ScatterStrategy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ScatterZipStrategy implements ScatterStrategy {

  @JsonProperty("scatterMethod")
  private ScatterMethod scatterMethod;
  @JsonProperty("combinations")
  private LinkedList<Combination> combinations = new LinkedList<>();
  
  @JsonProperty("values")
  private Map<String, LinkedList<Object>> values = new HashMap<>();
  @JsonProperty("indexes")
  private Map<String, LinkedList<Boolean>> indexes = new HashMap<>();

  public ScatterZipStrategy() {
    this.scatterMethod = ScatterMethod.dotproduct;
  }
  
  @JsonCreator
  public ScatterZipStrategy(@JsonProperty("scatterMethod") ScatterMethod scatterMethod, @JsonProperty("combinations") LinkedList<Combination> combinations, @JsonProperty("values") Map<String, LinkedList<Object>> values, @JsonProperty("indexes") Map<String, LinkedList<Boolean>> indexes) {
    super();
    this.scatterMethod = scatterMethod;
    this.combinations = combinations;
    this.values = values;
    this.indexes = indexes;
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
    @JsonProperty("position")
    private int position;
    @JsonProperty("enabled")
    private boolean enabled;

    @JsonCreator
    public Combination(@JsonProperty("position") int position, @JsonProperty("enabled") boolean enabled) {
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
