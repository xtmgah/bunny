package org.rabix.engine.model;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;

public class LinkRecord {

  private String contextId;
  
  private String sourceJobId;
  private String sourceJobPort;
  private LinkPortType sourceVarType;
  
  private String destinationJobId;
  private String destinationJobPort;
  private LinkPortType destinationVarType;

  private Integer position;
  
  public LinkRecord(String contextId, String sourceJobId, String sourceJobPort, LinkPortType sourceVarType, String destinationJobId, String destinationJobPort, LinkPortType destinationVarType, Integer position) {
    this.position = position;
    this.contextId = contextId;
    this.sourceJobId = sourceJobId;
    this.sourceJobPort = sourceJobPort;
    this.sourceVarType = sourceVarType;
    this.destinationJobId = destinationJobId;
    this.destinationJobPort = destinationJobPort;
    this.destinationVarType = destinationVarType;
  }

  public String getContextId() {
    return contextId;
  }
  
  public Integer getPosition() {
    return position;
  }
  
  public String getSourceJobId() {
    return sourceJobId;
  }

  public void setSourceJobId(String sourceJobId) {
    this.sourceJobId = sourceJobId;
  }

  public String getSourceJobPort() {
    return sourceJobPort;
  }

  public void setSourceJobPort(String sourceJobPort) {
    this.sourceJobPort = sourceJobPort;
  }

  public LinkPortType getSourceVarType() {
    return sourceVarType;
  }

  public void setSourceVarType(LinkPortType sourceVarType) {
    this.sourceVarType = sourceVarType;
  }

  public String getDestinationJobId() {
    return destinationJobId;
  }

  public void setDestinationJobId(String destinationJobId) {
    this.destinationJobId = destinationJobId;
  }

  public String getDestinationJobPort() {
    return destinationJobPort;
  }

  public void setDestinationJobPort(String destinationJobPort) {
    this.destinationJobPort = destinationJobPort;
  }

  public LinkPortType getDestinationVarType() {
    return destinationVarType;
  }

  public void setDestinationVarType(LinkPortType destinationVarType) {
    this.destinationVarType = destinationVarType;
  }

  @Override
  public String toString() {
    return "LinkRecord [contextId=" + contextId + ", sourceJobId=" + sourceJobId + ", sourceJobPort=" + sourceJobPort + ", sourceVarType=" + sourceVarType + ", destinationJobId=" + destinationJobId + ", destinationJobPort=" + destinationJobPort + ", destinationVarType=" + destinationVarType + ", position=" + position + "]";
  }

}
