package org.rabix.engine.model;

import java.util.ArrayList;
import java.util.List;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;

public class VariableRecord {

  private String contextId;
  
  private String jobId;
  private String portId;
  private LinkPortType type;
  private Object value;
  
  private boolean isWrapped;        // is value wrapped into array?
  private int numberOfGlobals;      // number of 'global' outputs if node is scattered 

  public VariableRecord(String contextId, String jobId, String portId, LinkPortType type, Object value) {
    this.contextId = contextId;
    
    this.jobId = jobId;
    this.portId = portId;
    this.type = type;
    this.value = value;
  }

  public String getContextId() {
    return contextId;
  }
  
  @SuppressWarnings("unchecked")
  public void addValue(Object value) {
    if (this.value == null) {
      this.value = value;
      return;
    }
    if (isWrapped) {
      ((List<Object>) this.value).add(value);
    } else {
      List<Object> valueList = new ArrayList<>();
      valueList.add(this.value);
      valueList.add(value);
      this.value = valueList;
      this.isWrapped = true;
    }
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
  
  
}
