package org.rabix.engine.rest.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendDispatcher {

  private int position = 0;

  private final static Logger logger = LoggerFactory.getLogger(BackendDispatcher.class);
  
  private final static long HEARTBEAT_PERIOD = TimeUnit.MINUTES.toMillis(5);

  private final List<BackendStub> backendStubs = new ArrayList<>();
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
      freeJobs.addAll(jobs);
      
      if (backendStubs.isEmpty()) {
        return false;
      }
      
      Iterator<Job> freeJobIterator = freeJobs.iterator();
      while(freeJobIterator.hasNext()) {
        Job freeJob = freeJobIterator.next();
        
        if (jobBackendMapping.containsKey(freeJob)) {
          freeJobIterator.remove();
          continue;
        }
        BackendStub backendStub = nextBackend();
        
        freeJobIterator.remove();
        jobBackendMapping.put(freeJob, backendStub.getBackend().getId());
        backendStub.send(freeJob);
        logger.info("Job {} sent to {}.", freeJob.getId(), backendStub.getBackend().getId());
      }
      return true;
    } finally {
      dispatcherLock.unlock();
    }
  }

  public void addBackendStub(BackendStub backendStub) {
    try {
      dispatcherLock.lock();
      this.backendStubs.add(backendStub);
      this.heartbeatInfo.put(backendStub.getBackend().getId(), System.currentTimeMillis());
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

  private BackendStub nextBackend() {
    BackendStub backendStub = backendStubs.get(position % backendStubs.size());
    position = (position + 1) % backendStubs.size();
    return backendStub;
  }

  private class HeartbeatMonitor implements Runnable {
    @Override
    public void run() {
      try {
        dispatcherLock.lock();
        
        long currentTime = System.currentTimeMillis();
        for (BackendStub backendStub : backendStubs) {
          Backend backend = backendStub.getBackend();

          HeartbeatInfo result = backendStub.getHeartbeat();
          if (result != null) {
            heartbeatInfo.put(result.getId(), result.getTimestamp());
          }
          if (currentTime - heartbeatInfo.get(backend.getId()) > HEARTBEAT_PERIOD) {
            backendStub.stop();
            backendStubs.remove(backendStub);
            
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
