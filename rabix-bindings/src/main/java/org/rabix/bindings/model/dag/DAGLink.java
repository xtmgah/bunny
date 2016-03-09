package org.rabix.bindings.model.dag;

public class DAGLink {

  private final DAGLinkPort source;
  private final DAGLinkPort destination;

  public DAGLink(DAGLinkPort source, DAGLinkPort destination) {
    this.source = source;
    this.destination = destination;
  }

  public DAGLinkPort getSource() {
    return source;
  }

  public DAGLinkPort getDestination() {
    return destination;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((destination == null) ? 0 : destination.hashCode());
    result = prime * result + ((source == null) ? 0 : source.hashCode());
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
    DAGLink other = (DAGLink) obj;
    if (destination == null) {
      if (other.destination != null)
        return false;
    } else if (!destination.equals(other.destination))
      return false;
    if (source == null) {
      if (other.source != null)
        return false;
    } else if (!source.equals(other.source))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "DAGLink [source=" + source + ", destination=" + destination + "]";
  }

}
