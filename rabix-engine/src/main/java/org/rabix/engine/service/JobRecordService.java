package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.rabix.engine.model.JobRecord;

public class JobRecordService {

  public static enum JobState {
    PENDING,
    READY,
    RUNNING,
    COMPLETED,
    FAILED
  }

  private Map<String, List<JobRecord>> jobRecordsPerContext = new HashMap<String, List<JobRecord>>();

  public static String generateUniqueId() {
    return UUID.randomUUID().toString();
  }
  
  public synchronized void create(JobRecord jobRecord) {
    getJobRecords(jobRecord.getRootId()).add(jobRecord);
  }

  public synchronized void update(JobRecord jobRecord) {
    for (JobRecord jr : getJobRecords(jobRecord.getRootId())) {
      if (jr.getId().equals(jobRecord.getId())) {
        jr.setState(jobRecord.getState());
        jr.setContainer(jobRecord.isContainer());
        jr.setScattered(jobRecord.isScattered());
        jr.setInputCounters(jobRecord.getInputCounters());
        jr.setOutputCounters(jobRecord.getOutputCounters());
        jr.setScatterWrapper(jobRecord.isScatterWrapper());
        jr.setScatterStrategy(jobRecord.getScatterStrategy());
        return;
      }
    }
  }
  
  public synchronized List<JobRecord> find(String contextId) {
    return getJobRecords(contextId);
  }
  
  public synchronized List<JobRecord> findReady(String contextId) {
    List<JobRecord> result = new ArrayList<>();
    
    for (JobRecord jr : getJobRecords(contextId)) {
      if (jr.getState().equals(JobState.READY) && jr.getRootId().equals(contextId)) {
        result.add(jr);
      }
    }
    return result;
  }
  
  public synchronized JobRecord find(String id, String contextId) {
    for (JobRecord jr : getJobRecords(contextId)) {
      if (jr.getId().equals(id) && jr.getRootId().equals(contextId)) {
        return jr;
      }
    }
    return null;
  }
  
  public synchronized JobRecord findRoot(String contextId) {
    for (JobRecord jr : getJobRecords(contextId)) {
      if (jr.isMaster() && jr.getRootId().equals(contextId)) {
        return jr;
      }
    }
    return null;
  }
  
  private synchronized List<JobRecord> getJobRecords(String contextId) {
    List<JobRecord> jobRecordList = jobRecordsPerContext.get(contextId);
    if (jobRecordList == null) {
      jobRecordList = new ArrayList<>();
      jobRecordsPerContext.put(contextId, jobRecordList);
    }
    return jobRecordList;
  }
  
}
