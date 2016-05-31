package org.rabix.transport.mechanism.impl.activemq;

import org.rabix.transport.mechanism.TransportQueue;

public class TransportQueueActiveMQ implements TransportQueue {

  private final String queue;
  
  public TransportQueueActiveMQ(String queue) {
    this.queue = queue;
  }
  
  public String getQueue() {
    return queue;
  }
}
