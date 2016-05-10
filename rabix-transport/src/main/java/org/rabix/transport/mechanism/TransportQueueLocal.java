package org.rabix.transport.mechanism;

public class TransportQueueLocal implements TransportQueue {

  private final String queue;
  
  public TransportQueueLocal(String queue) {
    this.queue = queue;
  }
  
  public String getQueue() {
    return queue;
  }
}
