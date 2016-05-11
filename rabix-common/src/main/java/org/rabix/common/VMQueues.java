package org.rabix.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class VMQueues {

  public final static String SEND_TO_BACKEND_QUEUE = "toBackendQueue";
  public final static String RECEIVE_FROM_BACKEND_QUEUE = "fromBackendQueue";
  public final static String RECEIVE_FROM_BACKEND_HEARTBEAT_QUEUE = "fromBackendHeartbeatQueue";
  
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
