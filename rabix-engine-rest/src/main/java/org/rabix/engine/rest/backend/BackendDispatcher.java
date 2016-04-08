package org.rabix.engine.rest.backend;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.rabix.bindings.model.Job;
import org.rabix.engine.rest.backend.impl.BackendMQ;

public class BackendDispatcher {

  private int position = 0;
  private final List<BackendMQ> backendStubs = new ArrayList<>();

  private final Set<Job> freeJobs = new HashSet<>();
  private final ConcurrentMap<String, String> jobBackendMapping = new ConcurrentHashMap<>();

  private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

  public BackendDispatcher() {
    start();
  }

  private synchronized void start() {
    executorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        if (!backendStubs.isEmpty()) {
          send(freeJobs);
          freeJobs.clear();
        }
      }
    }, 0, 1, TimeUnit.SECONDS);
  }

  public synchronized boolean send(Set<Job> jobs) {
    if (backendStubs.isEmpty()) {
      freeJobs.addAll(jobs);
      return true;
    }
    for (Job job : jobs) {
      if (jobBackendMapping.containsKey(job.getId())) {
        continue;
      }
      BackendMQ backendMQ = nextBackend();
      jobBackendMapping.put(job.getId(), backendMQ.getBackend().getId());

      backendMQ.send(job);
    }
    return true;
  }

  private synchronized BackendMQ nextBackend() {
    BackendMQ backendMQ = backendStubs.get(position % backendStubs.size());
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
