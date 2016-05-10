package org.rabix.transport.mechanism;

public class TransportQueueRabbitMQ implements TransportQueue {

  private final String exchange;
  private final String exchangeType;
  private final String routingKey;
  
  public TransportQueueRabbitMQ(String exchange, String exchangeType, String routingKey) {
    this.exchange = exchange;
    this.exchangeType = exchangeType;
    this.routingKey = routingKey;
  }
  
  public String getExchange() {
    return exchange;
  }
  
  public String getExchangeType() {
    return exchangeType;
  }
  
  public String getRoutingKey() {
    return routingKey;
  }
  
}