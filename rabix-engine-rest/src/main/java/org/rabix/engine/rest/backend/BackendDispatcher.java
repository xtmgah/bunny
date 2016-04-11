package org.rabix.engine.rest.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.engine.rest.backend.impl.BackendMQ;
import org.rabix.engine.rest.backend.impl.BackendMQ.HeartbeatInfo;
import org.rabix.engine.rest.model.Backend;
import org.rabix.engine.rest.transport.impl.TransportPluginMQ.ResultPair;

public class BackendDispatcher {

  private int position = 0;

  private final static long HEARTBEAT_PERIOD = TimeUnit.MINUTES.toMillis(5);

  private final List<BackendMQ> backendStubs = new ArrayList<>();
  private final Map<String, Long> heartbeatInfo = new HashMap<>();

  private final Set<Job> freeJobs = new HashSet<>();
  private final ConcurrentMap<Job, String> jobBackendMapping = new ConcurrentHashMap<>();

  private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  private ScheduledExecutorService heartbeatService = Executors.newSingleThreadScheduledExecutor();

  private Lock dispatcherLock = new ReentrantLock(true);

  public BackendDispatcher() {
    start();
  }

  private synchronized void start() {
    executorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          dispatcherLock.lock();
          if (!backendStubs.isEmpty()) {
            send(freeJobs);
            freeJobs.clear();
          }
        } finally {
          dispatcherLock.unlock();
        }
      }
    }, 0, 1, TimeUnit.SECONDS);

    heartbeatService.scheduleAtFixedRate(new HeartbeatMonitor(), 0, 15, TimeUnit.SECONDS);
  }

  public boolean send(Set<Job> jobs) {
    try {
      dispatcherLock.lock();
      if (backendStubs.isEmpty()) {
        freeJobs.addAll(jobs);
        return true;
      }
      for (Job job : jobs) {
        if (jobBackendMapping.containsKey(job.getId())) {
          continue;
        }
        BackendMQ backendMQ = nextBackend();
        jobBackendMapping.put(job, backendMQ.getBackend().getId());

        backendMQ.send(job);
      }
      return true;
    } finally {
      dispatcherLock.unlock();
    }
  }

  public void addBackendMQ(BackendMQ backendMQ) {
    try {
      dispatcherLock.lock();
      this.backendStubs.add(backendMQ);
      this.heartbeatInfo.put(backendMQ.getBackend().getId(), System.currentTimeMillis());
    } finally {
      dispatcherLock.unlock();
    }
  }

  public void remove(Job job) {
    try {
      dispatcherLock.lock();
      this.jobBackendMapping.remove(job.getId());
    } finally {
      dispatcherLock.unlock();
    }
  }

  private BackendMQ nextBackend() {
    BackendMQ backendMQ = backendStubs.get(position % backendStubs.size());
    position = (position + 1) % backendStubs.size();
    return backendMQ;
  }

  private class HeartbeatMonitor implements Runnable {
    @Override
    public void run() {
      try {
        dispatcherLock.lock();
        
        long currentTime = System.currentTimeMillis();
        for (BackendMQ backendMQ : backendStubs) {
          Backend backend = backendMQ.getBackend();

          ResultPair<HeartbeatInfo> result = backendMQ.receive(backend.getHeartbeatQueue(), HeartbeatInfo.class);
          if (result.isSuccess() && result.getResult() != null) {
            heartbeatInfo.put(result.getResult().getId(), result.getResult().getTimestamp());
          }
          if (currentTime - heartbeatInfo.get(backend.getId()) > HEARTBEAT_PERIOD) {
            backendMQ.stopConsumer();
            backendStubs.remove(backendMQ);
            
            List<Job> jobsToRemove = new ArrayList<>();
            for (Entry<Job, String> jobBackendEntry : jobBackendMapping.entrySet()) {
              if (jobBackendEntry.getValue().equals(backend.getId())) {
                jobsToRemove.add(jobBackendEntry.getKey());
              }
            }
            for (Job job : jobsToRemove) {
              jobBackendMapping.remove(job);
              freeJobs.add(Job.cloneWithStatus(job, JobStatus.READY));
            }
          }
        }
      } finally {
        dispatcherLock.unlock();
      }
    }

  }

}
