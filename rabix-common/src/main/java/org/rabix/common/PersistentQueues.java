package org.rabix.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;

import com.google.inject.Inject;
import com.leansoft.bigqueue.BigQueueImpl;
import com.leansoft.bigqueue.IBigQueue;

public class PersistentQueues {

  private final Configuration configuration;

  private static Map<String, IBigQueue> queues = new HashMap<>();
  
  @Inject
  public PersistentQueues(Configuration configuration) {
    this.configuration = configuration;
  }
  
  public synchronized IBigQueue getQueue(String name) throws IOException {
    IBigQueue queue = queues.get(name);
    if (queue == null) {
      String persistentQueuesDir = configuration.getString("persistentQueues.directory");
      queue = new BigQueueImpl(persistentQueuesDir, name);
      queues.put(name, queue);
    }
    return queue;
  }
  
}
