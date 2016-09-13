package org.rabix.bindings.cwl1.resolver;

import com.fasterxml.jackson.databind.JsonNode;

public class CWL1DocumentResolverReference {

  private boolean isResolving;
  private JsonNode resolvedNode;

  public CWL1DocumentResolverReference() {
  }
  
  public CWL1DocumentResolverReference(boolean isResolving, JsonNode resolvedNode) {
    this.isResolving = isResolving;
    this.resolvedNode = resolvedNode;
  }

  public boolean isResolving() {
    return isResolving;
  }

  public void setResolving(boolean isResolving) {
    this.isResolving = isResolving;
  }

  public JsonNode getResolvedNode() {
    return resolvedNode;
  }

  public void setResolvedNode(JsonNode resolvedNode) {
    this.resolvedNode = resolvedNode;
  }

  @Override
  public String toString() {
    return "CWL1DocumentResolverReference [isResolving=" + isResolving + ", resolvedNode=" + resolvedNode + "]";
  }
  
}
