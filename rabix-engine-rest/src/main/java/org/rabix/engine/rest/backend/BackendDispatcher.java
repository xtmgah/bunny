package org.rabix.engine.rest.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.rabix.bindings.model.Job;
import org.rabix.engine.rest.backend.impl.BackendMQ;
import org.rabix.engine.rest.transport.TransportPluginException;

public class BackendDispatcher {

  private int position = 0;
  private final List<BackendMQ> backendStubs = new ArrayList<>();

  private final ConcurrentMap<String, String> jobBackendMapping = new ConcurrentHashMap<>();

  public synchronized void send(Set<Job> jobs) throws TransportPluginException {
    for (Job job : jobs) {
      if (jobBackendMapping.containsKey(job.getId())) {
        continue;
      }
      BackendMQ backendMQ = nextBackend();
      jobBackendMapping.put(job.getId(), backendMQ.getBackend().getId());
      backendMQ.send(job);
    }
  }

  private synchronized BackendMQ nextBackend() {
    BackendMQ backendMQ = backendStubs.get(position);
    position = (position + 1) % backendStubs.size();
    return backendMQ;
  }

  public synchronized void addBackendMQ(BackendMQ backendMQ) {
    backendStubs.add(backendMQ);
  }
  
  public synchronized void remove(Job job) {
    this.jobBackendMapping.remove(job.getId());
  }

}
