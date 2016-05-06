package org.rabix.engine.rest.backend;

public interface Backend {

  public static enum BackendType {
    LOCAL,
    ACTIVE_MQ,
    RABBIT_MQ
  }

  String getId();
  
  void setId(String id);
  
  BackendType getType();

}
