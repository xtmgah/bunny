package org.rabix.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class VMQueues {

  public final static String SEND_QUEUE = "sendQueue";
  public final static String RECEIVE_QUEUE = "receiveQueue";
  public final static String HEARTBEAT_QUEUE = "heartbeatQueue";
  
  private static Map<String, Queue<Object>> queues = new HashMap<>();
  
  @SuppressWarnings("unchecked")
  public synchronized static <T> Queue<T> getQueue(String name) {
    Queue<Object> queue = queues.get(name);
    if (queue == null) {
      queue = new LinkedBlockingQueue<>();
      queues.put(name, queue);
    }
    return (Queue<T>) queue;
  }
  
}
