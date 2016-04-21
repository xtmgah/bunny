package org.rabix.engine.rest.backend;

import org.rabix.engine.rest.backend.impl.BackendLocal;
import org.rabix.engine.rest.backend.impl.BackendMQ;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
    @Type(value = BackendMQ.class, name = "MQ"),
    @Type(value = BackendLocal.class, name = "LOCAL") })
@JsonInclude(Include.NON_NULL)
public interface Backend {

  public static enum BackendType {
    MQ,
    LOCAL
  }

  String getId();
  
  void setId(String id);
  
  BackendType getType();

}
