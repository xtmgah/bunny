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

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.common.engine.control.EngineControlStopMessage;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.transport.backend.Backend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class BackendDispatcher {

  private final static Logger logger = LoggerFactory.getLogger(BackendDispatcher.class);

  private final static long DEFAULT_HEARTBEAT_PERIOD = TimeUnit.MINUTES.toMillis(2);
  
  private final List<BackendStub<?,?,?>> backendStubs = new ArrayList<>();
  private final Map<String, Long> heartbeatInfo = new HashMap<>();

  private final Set<Job> freeJobs = new HashSet<>();
  private final ConcurrentMap<Job, String> jobBackendMapping = new ConcurrentHashMap<>();

  private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  private ScheduledExecutorService heartbeatService = Executors.newSingleThreadScheduledExecutor();

  private Lock dispatcherLock = new ReentrantLock(true);

  private int position = 0;
  
  private final long heartbeatPeriod;
  
  @Inject
  public BackendDispatcher(Configuration configuration) {
    this.heartbeatPeriod = configuration.getLong("backend.cleaner.heartbeatPeriodMills", DEFAULT_HEARTBEAT_PERIOD);
    start();
  }

  private synchronized void start() {
    executorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          dispatcherLock.lock();
          if (!backendStubs.isEmpty()) {
            send(freeJobs.toArray(new Job[] {}));
            freeJobs.clear();
          }
        } finally {
          dispatcherLock.unlock();
        }
      }
    }, 0, 10, TimeUnit.SECONDS);

    heartbeatService.scheduleAtFixedRate(new HeartbeatMonitor(), 0, heartbeatPeriod, TimeUnit.MILLISECONDS);
  }

  public boolean send(Job... jobs) {
    try {
      dispatcherLock.lock();
      for (Job job : jobs) {
        freeJobs.add(job);
      }

      if (backendStubs.isEmpty()) {
        return false;
      }

      Iterator<Job> freeJobIterator = freeJobs.iterator();
      while (freeJobIterator.hasNext()) {
        Job freeJob = freeJobIterator.next();

        if (jobBackendMapping.containsKey(freeJob)) {
          freeJobIterator.remove();
          continue;
        }
        BackendStub<?,?,?> backendStub = nextBackend();

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
  
  public boolean stop(Job... jobs) {
    try {
      dispatcherLock.lock();
      
      for (Job job : jobs) {
        String backendId = jobBackendMapping.get(job);
        if (backendId != null) {
          BackendStub<?,?,?> backendStub = getBackendStub(backendId);
          if (backendStub != null) {
            backendStub.send(new EngineControlStopMessage(job.getId(), job.getRootId()));
          }
        }
      }
      return true;
    } finally {
      dispatcherLock.unlock();
    }
  }

  public void addBackendStub(BackendStub<?,?,?> backendStub) {
    try {
      dispatcherLock.lock();
      backendStub.start(heartbeatInfo);
      this.backendStubs.add(backendStub);
      this.heartbeatInfo.put(backendStub.getBackend().getId(), System.currentTimeMillis());
    } finally {
      dispatcherLock.unlock();
    }
  }

  public void remove(Job job) {
    try {
      dispatcherLock.lock();
      this.jobBackendMapping.remove(job);
    } finally {
      dispatcherLock.unlock();
    }
  }

  private BackendStub<?,?,?> nextBackend() {
    BackendStub<?,?,?> backendStub = backendStubs.get(position % backendStubs.size());
    position = (position + 1) % backendStubs.size();
    return backendStub;
  }
  
  private BackendStub<?,?,?> getBackendStub(String id) {
    for (BackendStub<?,?,?> backendStub : backendStubs) {
      if (backendStub.getBackend().getId().equals(id)) {
        return backendStub;
      }
    }
    return null;
  }

  private class HeartbeatMonitor implements Runnable {
    @Override
    public void run() {
      try {
        dispatcherLock.lock();
        logger.info("Checking Backend heartbeats...");
        
        long currentTime = System.currentTimeMillis();
        for (BackendStub<?,?,?> backendStub : backendStubs) {
          Backend backend = backendStub.getBackend();

          if (currentTime - heartbeatInfo.get(backend.getId()) > heartbeatPeriod) {
            backendStub.stop();
            backendStubs.remove(backendStub);
            logger.info("Removing Backend {}", backendStub.getBackend().getId());
            
            List<Job> jobsToRemove = new ArrayList<>();
            for (Entry<Job, String> jobBackendEntry : jobBackendMapping.entrySet()) {
              if (jobBackendEntry.getValue().equals(backend.getId())) {
                jobsToRemove.add(jobBackendEntry.getKey());
              }
            }
            for (Job job : jobsToRemove) {
              jobBackendMapping.remove(job);
              freeJobs.add(Job.cloneWithStatus(job, JobStatus.READY));
              logger.info("Reassign Job {} to free Jobs", job.getId());
            }
          }
        }
        logger.info("Heartbeats checked");
      } finally {
        dispatcherLock.unlock();
      }
    }
  }

}
