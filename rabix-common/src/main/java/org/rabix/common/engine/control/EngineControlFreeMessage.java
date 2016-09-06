package org.rabix.common.engine.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EngineControlFreeMessage extends EngineControlMessage {

  @JsonCreator
  public EngineControlFreeMessage(@JsonProperty("rootId") String rootId) {
    super(rootId);
  }

  @Override
  @JsonIgnore
  public EngineControlMessageType getType() {
    return EngineControlMessageType.FREE;
  }

}
