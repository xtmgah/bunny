package org.rabix.engine.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;

public class VariableRecord {

  private String contextId;
  
  private String jobId;
  private String portId;
  private LinkPortType type;
  private Object value;
  
  private boolean isWrapped;        // is value wrapped into array?
  private int numberOfGlobals;      // number of 'global' outputs if node is scattered 

  private boolean isDefault;
  
  public VariableRecord(String contextId, String jobId, String portId, LinkPortType type, Object value) {
    this.jobId = jobId;
    this.portId = portId;
    this.type = type;
    this.value = value;
    this.contextId = contextId;
  }
  
  public VariableRecord(String contextId, String jobId, String portId, LinkPortType type, Object value, boolean isDefault) {
    this.jobId = jobId;
    this.portId = portId;
    this.type = type;
    this.value = value;
    this.isDefault = isDefault;
    this.contextId = contextId;
  }

  public String getContextId() {
    return contextId;
  }
  
  public void addValue(Object value, LinkMerge linkMerge, Integer position) {
    if (value == null) {
      return;
    }
    if (isDefault) {
      isDefault = false;
      this.value = null;
    }
    if (linkMerge == null) {
      linkMerge = LinkMerge.merge_nested;
    }
    switch (linkMerge) {
    case merge_nested:
      addMergeNested(value, position);
      break;
    case merge_flattened:
      addMergeFlattened(value, position);
    default:
      break;
    }
  }

  @SuppressWarnings("unchecked")
  private void addMergeNested(Object value, Integer position) {
    if (this.value == null) {
      if (position != null) {
        this.isWrapped = true;
        this.value = new ArrayList<>(position);
        ((List<Object>) this.value).set(position - 1, value);
      } else {
        this.value = value;
      }
      return;
    }
    if (isWrapped) {
      if (position != null) {
        expand((List<Object>) this.value, position);
        ((List<Object>) this.value).set(position - 1, value);
      } else {
        ((List<Object>) this.value).add(value);
      }
    } else {
      this.value = wrap(this.value, value);
      this.isWrapped = true;
    }
  }

  @SuppressWarnings("unchecked")
  private void addMergeFlattened(Object value, Integer position) {
    List<Object> flattened = flatten(value);
    
    if (this.value == null) {
      if (flattened.size() > 1) {
        this.value = flattened;
      } else {
        this.value = flattened.get(0);
      }
      return;
    }
    if (this.value instanceof List<?>) {
      ((List<Object>) this.value).addAll(flattened);
    } else {
      this.value = wrap(this.value);
      ((List<Object>) this.value).addAll(flattened);
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
  
  private List<Object> flatten(Object value) {
    List<Object> flattenedValues = new ArrayList<>();
    
    if (value instanceof List<?>) {
      for (Object subvalue : ((List<?>) value)) {
        flattenedValues.addAll((Collection<? extends Object>) flatten(subvalue));
      }
    } else {
      flattenedValues.add(value);
    }
    return flattenedValues;
  }
  
  @SuppressWarnings("unchecked")
  private <T> Collection<T> wrap(final T... objects){
    final Collection<T> collection = new ArrayList<T>();
    for (T t : objects) {
      collection.add(t);
    }
    return collection;
}
  
  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getPortId() {
    return portId;
  }

  public void setPortId(String portId) {
    this.portId = portId;
  }

  public LinkPortType getType() {
    return type;
  }

  public void setType(LinkPortType type) {
    this.type = type;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public boolean isWrapped() {
    return isWrapped;
  }

  public void setWrapped(boolean isWrapped) {
    this.isWrapped = isWrapped;
  }

  public int getNumberOfGlobals() {
    return numberOfGlobals;
  }

  public void setNumberGlobals(int numberOfGlobals) {
    this.numberOfGlobals = numberOfGlobals;
  }

  @Override
  public String toString() {
    return "VariableRecord [contextId=" + contextId + ", jobId=" + jobId + ", portId=" + portId + ", type=" + type + ", value=" + value + ", isWrapped=" + isWrapped + ", numberOfGlobals=" + numberOfGlobals + ", isDefault=" + isDefault + "]";
  }
  
}
