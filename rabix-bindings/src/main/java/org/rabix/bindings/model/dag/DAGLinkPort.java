package org.rabix.bindings.model.dag;

public class DAGLinkPort {

  public static enum LinkPortType {
    INPUT,
    OUTPUT
  }
  
  private final String id;
  private final String dagNodeId;
  private final LinkPortType type;
  private boolean scatter;
    
  public DAGLinkPort(String id, String dagNodeId, LinkPortType type, boolean scatter) {
    this.id = id;
    this.type = type;
    this.scatter = scatter;
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
  
  public String getNodeId() {
    return dagNodeId;
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
    result = prime * result + (scatter ? 1231 : 1237);
    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
    if (scatter != other.scatter)
      return false;
    if (type != other.type)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "DAGLinkPort [id=" + id + ", nodeId=" + dagNodeId + ", type=" + type + ", scatter=" + scatter + "]";
  }

}
