package org.rabix.engine.model.scatter.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.rabix.bindings.model.ScatterMethod;
import org.rabix.engine.model.scatter.ScatterStrategy;

public class ScatterCartesianStrategy implements ScatterStrategy {

  private ScatterMethod scatterMethod;
  private LinkedList<Combination> combinations = new LinkedList<>();

  private Map<String, LinkedList<Object>> values = new HashMap<>();
  private Map<String, LinkedList<Integer>> positions = new HashMap<>();

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
    private int position;
    private boolean enabled;
    private LinkedList<Integer> indexes;

    public Combination(int position, boolean enabled, LinkedList<Integer> indexes) {
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