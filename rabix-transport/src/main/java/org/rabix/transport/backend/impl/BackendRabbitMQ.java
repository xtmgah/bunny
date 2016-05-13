package org.rabix.transport.backend.impl;

import org.rabix.transport.backend.Backend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BackendRabbitMQ extends Backend {

  @JsonProperty("host")
  private String host;
  @JsonProperty("engineConfiguration")
  private EngineConfiguration engineConfiguration;
  @JsonProperty("backendConfiguration")
  private BackendConfiguration backendConfiguration;
  
  @JsonCreator
  public BackendRabbitMQ(@JsonProperty("id") String id, @JsonProperty("host") String host, @JsonProperty("engineConfiguration") EngineConfiguration engineConfiguration, @JsonProperty("backendConfiguration") BackendConfiguration backendConfiguration) {
    this.id = id;
    this.host = host;
    this.engineConfiguration = engineConfiguration;
    this.backendConfiguration = backendConfiguration;
  }
  
  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public EngineConfiguration getEngineConfiguration() {
    return engineConfiguration;
  }

  public void setEngineConfiguration(EngineConfiguration engineConfiguration) {
    this.engineConfiguration = engineConfiguration;
  }

  public BackendConfiguration getBackendConfiguration() {
    return backendConfiguration;
  }

  public void setBackendConfiguration(BackendConfiguration backendConfiguration) {
    this.backendConfiguration = backendConfiguration;
  }

  public static class EngineConfiguration {
    @JsonProperty("exchange")
    private String exchange;
    @JsonProperty("exchange_type")
    private String exchangeType;
    @JsonProperty("receive_routing_key")
    private String receiveRoutingKey;
    @JsonProperty("heartbeat_routing_key")
    private String heartbeatRoutingKey;
    
    @JsonCreator
    public EngineConfiguration(@JsonProperty("exchange") String exchange, @JsonProperty("exchange_type") String exchangeType, @JsonProperty("receive_routing_key") String receiveRoutingKey, @JsonProperty("heartbeat_routing_key") String heartbeatRoutingKey) {
      this.exchange = exchange;
      this.exchangeType = exchangeType;
      this.receiveRoutingKey = receiveRoutingKey;
      this.heartbeatRoutingKey = heartbeatRoutingKey;
    }

    public String getExchange() {
      return exchange;
    }

    public String getExchangeType() {
      return exchangeType;
    }

    public String getReceiveRoutingKey() {
      return receiveRoutingKey;
    }

    public String getHeartbeatRoutingKey() {
      return heartbeatRoutingKey;
    }
  }
  
  public static class BackendConfiguration {
    @JsonProperty("exchange")
    private String exchange;
    @JsonProperty("exchange_type")
    private String exchangeType;
    @JsonProperty("receive_routing_key")
    private String receiveRoutingKey;
    
    @JsonCreator
    public BackendConfiguration(@JsonProperty("exchange") String exchange, @JsonProperty("exchange_type") String exchangeType, @JsonProperty("receive_routing_key") String receiveRoutingKey) {
      this.exchange = exchange;
      this.exchangeType = exchangeType;
      this.receiveRoutingKey = receiveRoutingKey;
    }

    public String getExchange() {
      return exchange;
    }

    public String getExchangeType() {
      return exchangeType;
    }

    public String getReceiveRoutingKey() {
      return receiveRoutingKey;
    }
  }
  
  @Override
  @JsonIgnore
  public BackendType getType() {
    return BackendType.RABBIT_MQ;
  }

}