package org.rabix.engine.rest.backend;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.rabix.engine.rest.db.BackendRecord;
import org.rabix.engine.rest.db.BackendRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class BackendDispatcher {

  private int position = 0;

  private final static Logger logger = LoggerFactory.getLogger(BackendDispatcher.class);
  
  private final static long HEARTBEAT_PERIOD = TimeUnit.MINUTES.toMillis(1);

  private final List<BackendStub> backendStubs = new ArrayList<>();

  private final Set<Job> freeJobs = new HashSet<>();
  private final ConcurrentMap<Job, String> jobBackendMapping = new ConcurrentHashMap<>();

  private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  private ScheduledExecutorService heartbeatService = Executors.newSingleThreadScheduledExecutor();

  private Lock dispatcherLock = new ReentrantLock(true);

  private final BackendRecordRepository backendRecordRepository;

  @Inject
  public BackendDispatcher(BackendRecordRepository backendRecordRepository) {
    this.backendRecordRepository = backendRecordRepository;
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
    }, 0, 10, TimeUnit.SECONDS);

    heartbeatService.scheduleAtFixedRate(new HeartbeatMonitor(), 0, 20, TimeUnit.SECONDS);
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
      backendStub.start();
      this.backendStubs.add(backendStub);
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
        
        List<BackendRecord> backendRecords = backendRecordRepository.findActive();
        
        long currentTime = System.currentTimeMillis();
        for (BackendRecord backendRecord : backendRecords) {
          if (currentTime - backendRecord.getHeartbeatTime() > HEARTBEAT_PERIOD) {
            BackendStub backendStub = findBackendStub(backendRecord.getId());
            if (backendStub != null) {
              backendStub.stop();
              backendStubs.remove(backendStub);
            }
            
            backendRecord.setActive(false);
            backendRecordRepository.update(backendRecord);
            
            List<Job> jobsToRemove = new ArrayList<>();
            for (Entry<Job, String> jobBackendEntry : jobBackendMapping.entrySet()) {
              if (jobBackendEntry.getValue().equals(backendRecord.getId())) {
                jobsToRemove.add(jobBackendEntry.getKey());
              }
            }
            for (Job job : jobsToRemove) {
              jobBackendMapping.remove(job);
              freeJobs.add(Job.cloneWithStatus(job, JobStatus.READY));
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        logger.error("Failed to update Heartbeats", e);
      } finally {
        dispatcherLock.unlock();
      }
    }
    
    private BackendStub findBackendStub(String id) {
      for (BackendStub backendStub : backendStubs) {
        if (id.equals(backendStub.getBackend().getId())) {
          return backendStub;
        }
      }
      return null;
    }
  }

}
