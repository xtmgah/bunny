package org.rabix.engine.rest.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.model.Job;

public class JobDB {

  private Map<String, Job> jobs = new HashMap<>();
  
  public synchronized void add(Job job) {
    jobs.put(job.getId(), job);
  }
  
  public synchronized void update(Job job) {
    jobs.put(job.getId(), job);
  }
  
  public synchronized Job get(String id) {
    return jobs.get(id);
  }
  
  public synchronized Set<Job> getJobs() {
    Set<Job> jobSet = new HashSet<>();
    for (Job job : jobs.values()) {
      jobSet.add(job);
    }
    return jobSet;
  }
  
  public synchronized Set<Job> getJobs(String rootId) {
    Set<Job> jobSet = new HashSet<>();
    for (Job job : jobs.values()) {
      if (job.getRootId().equals(rootId)) {
        jobSet.add(job);
      }
    }
    return jobSet;
  }
}
