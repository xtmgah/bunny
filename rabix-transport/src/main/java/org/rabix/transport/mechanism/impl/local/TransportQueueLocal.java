package org.rabix.transport.mechanism.impl.local;

import org.rabix.transport.mechanism.TransportQueue;

public class TransportQueueLocal implements TransportQueue {

  private final String queue;
  
  public TransportQueueLocal(String queue) {
    this.queue = queue;
  }
  
  public String getQueue() {
    return queue;
  }
}
