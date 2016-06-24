package org.rabix.engine.rest.backend.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StopControlMessage {

  @JsonProperty("id")
  private String id;
  @JsonProperty("rootId")
  private String rootId;
  
  @JsonCreator
  public StopControlMessage(@JsonProperty("id") String id, @JsonProperty("rootId") String rootId) {
    this.id = id;
    this.rootId = rootId;
  }
  
  public String getId() {
    return id;
  }
  
  public String getRootId() {
    return rootId;
  }

  @Override
  public String toString() {
    return "StopControlMessage [id=" + id + ", rootId=" + rootId + "]";
  }
  
}
