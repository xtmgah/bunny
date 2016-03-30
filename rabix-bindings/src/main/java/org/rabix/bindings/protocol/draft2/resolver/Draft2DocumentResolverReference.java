package org.rabix.bindings.protocol.draft2.resolver;

import com.fasterxml.jackson.databind.JsonNode;

public class Draft2DocumentResolverReference {

  private boolean isResolving;
  private JsonNode resolvedNode;

  public Draft2DocumentResolverReference() {
  }
  
  public Draft2DocumentResolverReference(boolean isResolving, JsonNode resolvedNode) {
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
    return "Draft2DocumentResolverReference [isResolving=" + isResolving + ", resolvedNode=" + resolvedNode + "]";
  }
  
}
