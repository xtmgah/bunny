package org.rabix.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class VMQueues {

  public final static String SEND_TO_BACKEND_QUEUE = "toBackendQueue";
  public final static String SEND_TO_BACKEND_CONTROL_QUEUE = "toBackendControlQueue";
  public final static String RECEIVE_FROM_BACKEND_QUEUE = "fromBackendQueue";
  public final static String RECEIVE_FROM_BACKEND_HEARTBEAT_QUEUE = "fromBackendHeartbeatQueue";
  
  private static Map<String, BlockingQueue<Object>> queues = new HashMap<>();
  
  @SuppressWarnings("unchecked")
  public synchronized static <T> BlockingQueue<T> getQueue(String name) {
    BlockingQueue<Object> queue = queues.get(name);
    if (queue == null) {
      queue = new LinkedBlockingQueue<>();
      queues.put(name, queue);
    }
    return (BlockingQueue<T>) queue;
  }
  
}
