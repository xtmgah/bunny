package org.rabix.bindings.model.dag;

import java.util.List;

public class DAGContainer extends DAGNode {

  private final List<DAGLink> links;
  private final List<DAGNode> children;

  public DAGContainer(String id, List<DAGLinkPort> inputPorts, List<DAGLinkPort> outputPorts, Object app, ScatterMethod scatterMethod, LinkMerge linkMerge, List<DAGLink> links, List<DAGNode> children) {
    super(id, inputPorts, outputPorts, scatterMethod, linkMerge, app);
    this.links = links;
    this.children = children;
  }

  public List<DAGNode> getChildren() {
    return children;
  }

  public List<DAGLink> getLinks() {
    return links;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "DAGContainer [links=" + links + ", children=" + children + ", id=" + id + ", linkMerge=" + linkMerge + ", scatterMethod=" + scatterMethod + ", inputPorts=" + inputPorts + ", outputPorts=" + outputPorts + "]";
  }

}
