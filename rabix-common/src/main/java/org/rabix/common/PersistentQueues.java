package org.rabix.common;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;

import com.google.inject.Inject;
import com.leansoft.bigqueue.BigQueueImpl;
import com.leansoft.bigqueue.IBigQueue;

public class PersistentQueues {

  private final Configuration configuration;

  @Inject
  public PersistentQueues(Configuration configuration) {
    this.configuration = configuration;
  }
  
  public IBigQueue getQueue(String name) throws IOException {
    String persistentQueuesDir = configuration.getString("persistentQueues.directory");
    return new BigQueueImpl(persistentQueuesDir, name);
  }
  
}
