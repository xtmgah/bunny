package org.rabix.bindings.model.dag;

import org.rabix.bindings.model.LinkMerge;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DAGLinkPort {

  public static enum LinkPortType {
    INPUT,
    OUTPUT
  }
  
  @JsonProperty("id")
  private String id;
  @JsonProperty("dagNodeId")
  private String dagNodeId;
  @JsonProperty("type")
  private LinkPortType type;
  @JsonProperty("scatter")
  private boolean scatter;
  
  private LinkMerge linkMerge;
    
  @JsonCreator
  public DAGLinkPort(@JsonProperty("id") String id, @JsonProperty("dagNodeId") String dagNodeId, @JsonProperty("type") LinkPortType type, @JsonProperty("linkMerge") LinkMerge linkMerge, @JsonProperty("scatter") boolean scatter) {
    this.id = id;
    this.type = type;
    this.scatter = scatter;
    this.linkMerge = linkMerge;
    this.dagNodeId = dagNodeId;
  }
  
  public String getId() {
    return id;
  }

  public boolean isScatter() {
    return scatter;
  }
  
  public void setScatter(boolean scatter) {
    this.scatter = scatter;
  }
  
  public String getDagNodeId() {
    return dagNodeId;
  }

  public LinkMerge getLinkMerge() {
    return linkMerge;
  }
  
  public void setLinkMerge(LinkMerge linkMerge) {
    this.linkMerge = linkMerge;
  }
  
  public LinkPortType getType() {
    return type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dagNodeId == null) ? 0 : dagNodeId.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DAGLinkPort other = (DAGLinkPort) obj;
    if (dagNodeId == null) {
      if (other.dagNodeId != null)
        return false;
    } else if (!dagNodeId.equals(other.dagNodeId))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "DAGLinkPort [id=" + id + ", dagNodeId=" + dagNodeId + ", type=" + type + ", scatter=" + scatter + ", linkMerge=" + linkMerge + "]";
  }
  
}
