package org.rabix.engine.event.impl;

import java.util.Map;

import org.rabix.bindings.model.Context;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.event.Event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This event is a starter event. It triggers the algorithm start. 
 */
public class InitEvent implements Event {

  @JsonProperty("node")
  private final DAGNode node;
  @JsonProperty("value")
  private final Map<String, Object> value;
  @JsonProperty("rootId")
  private final String rootId;
  @JsonProperty("context")
  private final Context context;
  
  @JsonCreator
  public InitEvent(@JsonProperty("context") Context context, @JsonProperty("rootId") String rootId, @JsonProperty("node") DAGNode node, @JsonProperty("value") Map<String, Object> value) {
    this.node = node;
    this.value = value;
    this.rootId = rootId;
    this.context = context;
  }

  public DAGNode getNode() {
    return node;
  }

  public Map<String, Object> getValue() {
    return value;
  }
  
  public String getRootId() {
    return rootId;
  }
  
  @Override
  public String getContextId() {
    return context.getId();
  }
  
  public Context getContext() {
    return context;
  }

  @Override
  public EventType getType() {
    return EventType.INIT;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((context == null) ? 0 : context.hashCode());
    result = prime * result + ((node == null) ? 0 : node.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    InitEvent other = (InitEvent) obj;
    if (context == null) {
      if (other.context != null)
        return false;
    } else if (!context.equals(other.context))
      return false;
    if (node == null) {
      if (other.node != null)
        return false;
    } else if (!node.equals(other.node))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  public String toString() {
    return "InitEvent [context=" + context + ", node=" + node + ", value=" + value + "]";
  }

}
