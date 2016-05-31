package org.rabix.engine.model.scatter.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.rabix.bindings.model.ScatterMethod;
import org.rabix.engine.model.scatter.ScatterStrategy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ScatterCartesianStrategy implements ScatterStrategy {

  @JsonProperty("scatterMethod")
  private ScatterMethod scatterMethod;
  @JsonProperty("combinations")
  private LinkedList<Combination> combinations = new LinkedList<>();

  @JsonProperty("values")
  private Map<String, LinkedList<Object>> values = new HashMap<>();
  @JsonProperty("positions")
  private Map<String, LinkedList<Integer>> positions = new HashMap<>();

  @JsonCreator
  public ScatterCartesianStrategy(@JsonProperty("scatterMethod") ScatterMethod scatterMethod,
      @JsonProperty("combinations") LinkedList<Combination> combinations,
      @JsonProperty("values") Map<String, LinkedList<Object>> values,
      @JsonProperty("positions") Map<String, LinkedList<Integer>> positions) {
    this.scatterMethod = scatterMethod;
    this.combinations = combinations;
    this.values = values;
    this.positions = positions;
  }

  public ScatterCartesianStrategy(ScatterMethod scatterMethod) {
    this.scatterMethod = scatterMethod;
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

  public Map<String, LinkedList<Integer>> getPositions() {
    return positions;
  }

  public void setPositions(Map<String, LinkedList<Integer>> positions) {
    this.positions = positions;
  }

  public static class Combination {
    @JsonProperty("position")
    private int position;
    @JsonProperty("enabled")
    private boolean enabled;
    @JsonProperty("indexes")
    private LinkedList<Integer> indexes;

    @JsonCreator
    public Combination(@JsonProperty("position") int position, @JsonProperty("enabled") boolean enabled, @JsonProperty("indexes") LinkedList<Integer> indexes) {
      this.position = position;
      this.enabled = enabled;
      this.indexes = indexes;
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

    public LinkedList<Integer> getIndexes() {
      return indexes;
    }

    public void setIndexes(LinkedList<Integer> indexes) {
      this.indexes = indexes;
    }

    @Override
    public String toString() {
      return "Combination [position=" + position + ", enabled=" + enabled + ", indexes=" + indexes + "]";
    }
  }
}