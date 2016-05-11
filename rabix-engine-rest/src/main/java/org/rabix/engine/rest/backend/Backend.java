package org.rabix.engine.rest.backend;

public interface Backend {

  public static enum BackendType {
    MQ,
    LOCAL
  }

  String getId();
  
  void setId(String id);
  
  BackendType getType();

}
