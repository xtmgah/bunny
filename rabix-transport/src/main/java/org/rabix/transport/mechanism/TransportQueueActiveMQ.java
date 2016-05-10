package org.rabix.transport.mechanism;

public class TransportQueueActiveMQ implements TransportQueue {

  private final String queue;
  
  public TransportQueueActiveMQ(String queue) {
    this.queue = queue;
  }
  
  public String getQueue() {
    return queue;
  }
}
