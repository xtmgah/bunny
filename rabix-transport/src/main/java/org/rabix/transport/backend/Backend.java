package org.rabix.transport.backend;

import org.rabix.transport.backend.impl.BackendActiveMQ;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.backend.impl.BackendRabbitMQ;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
    @Type(value = BackendActiveMQ.class, name = "ACTIVE_MQ"),
    @Type(value = BackendRabbitMQ.class, name = "RABBIT_MQ"),
    @Type(value = BackendLocal.class, name = "LOCAL") })
@JsonInclude(Include.NON_NULL)
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
