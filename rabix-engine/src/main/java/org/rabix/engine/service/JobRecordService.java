package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.JobRecord.PortCounter;

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
  
  public PortCounter getInputCounter(JobRecord jobRecord, String port) {
    for (PortCounter portCounter : jobRecord.getInputCounters()) {
      if (portCounter.getPort().equals(port)) {
        return portCounter;
      }
    }
    return null;
  }
  
  public PortCounter getOutputCounter(JobRecord jobRecord, String port) {
    for (PortCounter portCounter : jobRecord.getOutputCounters()) {
      if (portCounter.getPort().equals(port)) {
        return portCounter;
      }
    }
    return null;
  }
  
  public int getInputPortIncoming(JobRecord jobRecord, String port) {
    for (PortCounter pc : jobRecord.getInputCounters()) {
      if (pc.getPort().equals(port)) {
        return pc.getIncoming();
      }
    }
    return 0;
  }
  
  public boolean isInputPortBlocking(JobRecord jobRecord, DAGNode node, String port) {
    return getInputPortIncoming(jobRecord, port) > 1 && LinkMerge.isBlocking(node.getLinkMerge(port, LinkPortType.INPUT));
  }
  
  public int getOutputPortIncoming(JobRecord jobRecord,String port) {
    for (PortCounter pc : jobRecord.getOutputCounters()) {
      if (pc.getPort().equals(port)) {
        return pc.getIncoming();
      }
    }
    return 0;
  }
  
  public boolean isInputPortReady(JobRecord jobRecord,String port) {
    for (PortCounter pc : jobRecord.getInputCounters()) {
      if (pc.getPort().equals(port)) {
        if (pc.getCounter() == 0) {
          return true;
        }
      }
    }
    return false;
  }
  
  public boolean isOutputPortReady(JobRecord jobRecord, String port) {
    for (PortCounter pc : jobRecord.getOutputCounters()) {
      if (pc.getPort().equals(port)) {
        if (pc.getCounter() == 0) {
          return true;
        }
      }
    }
    return false;
  }

  public void incrementPortCounter(JobRecord jobRecord, DAGLinkPort port, LinkPortType type) {
    List<PortCounter> counters = type.equals(LinkPortType.INPUT) ? jobRecord.getInputCounters() : jobRecord.getOutputCounters();

    for (PortCounter pc : counters) {
      if (pc.getPort().equals(port.getId())) {
        pc.setCounter(pc.getCounter() + 1);
        return;
      }
    }
    PortCounter portCounter = new PortCounter(port.getId(), 1, port.isScatter());
    counters.add(portCounter);
  }
  
  public void incrementPortCounterIfThereIsNo(JobRecord jobRecord, DAGLinkPort port, LinkPortType type) {
    List<PortCounter> counters = type.equals(LinkPortType.INPUT) ? jobRecord.getInputCounters() : jobRecord.getOutputCounters();

    boolean exists = false;
    for (PortCounter pc : counters) {
      if (pc.getPort().equals(port.getId())) {
        exists = true;
        if (pc.getCounter() < 1) {
          pc.setCounter(pc.getCounter() + 1);
          return;
        }
      }
    }
    if (!exists) {
      PortCounter portCounter = new PortCounter(port.getId(), 1, port.isScatter());
      counters.add(portCounter);
    }
  }
  
  public void decrementPortCounter(JobRecord jobRecord, String portId, LinkPortType type) {
    List<PortCounter> counters = type.equals(LinkPortType.INPUT) ? jobRecord.getInputCounters() : jobRecord.getOutputCounters();
    for (PortCounter portCounter : counters) {
      if (portCounter.getPort().equals(portId)) {
        portCounter.setCounter(portCounter.getCounter() - 1);
      }
    }
  }
  
  public void resetInputPortCounters(JobRecord jobRecord, int value) {
    if (jobRecord.getNumberOfGlobalInputs() == value) {
      return;
    }
    int oldValue = jobRecord.getNumberOfGlobalInputs();
    if (jobRecord.getNumberOfGlobalInputs() < value) {
      jobRecord.setNumberOfGlobalInputs(value);

      for (PortCounter pc : jobRecord.getInputCounters()) {
        if (pc.getCounter() != value) {
          if (pc.getCounter() == 0) {
            continue;
          }
          if (oldValue != 0) {
            pc.setCounter(jobRecord.getNumberOfGlobalInputs() - (oldValue - pc.getCounter()));
          } else {
            pc.setCounter(jobRecord.getNumberOfGlobalInputs());
          }
        }
      }
    }
  }

  public void resetOutputPortCounters(JobRecord jobRecord, int value) {
    if (jobRecord.getNumberOfGlobalOutputs() == value) {
      return;
    }
    int oldValue = jobRecord.getNumberOfGlobalOutputs();
    if (jobRecord.getNumberOfGlobalOutputs() < value) {
      jobRecord.setNumberOfGlobalOutputs(value);

      for (PortCounter pc : jobRecord.getOutputCounters()) {
        if (pc.getCounter() == 0) {
          continue;
        }
        if (pc.getCounter() != value) {
          if (oldValue != 0) {
            pc.setCounter(jobRecord.getNumberOfGlobalOutputs() - (oldValue - pc.getCounter()));
          } else {
            pc.setCounter(jobRecord.getNumberOfGlobalOutputs());
          }
        }
      }
    }
  }

  public boolean isReady(JobRecord jobRecord) {
    for (PortCounter portCounter : jobRecord.getInputCounters()) {
      if (portCounter.getCounter() > 0) {
        return false;
      }
    }
    return true;
  }

  public boolean isCompleted(JobRecord jobRecord) {
    for (PortCounter portCounter : jobRecord.getOutputCounters()) {
      if (portCounter.getCounter() > 0) {
        return false;
      }
    }
    return true;
  }

  public boolean isScatterPort(JobRecord jobRecord, String port) {
    for (PortCounter portCounter : jobRecord.getInputCounters()) {
      if (portCounter.getPort().equals(port)) {
        return portCounter.isScatter();
      }
    }
    return false;
  }

  public List<String> getScatterPorts(JobRecord jobRecord) {
    List<String> result = new ArrayList<>();

    for (PortCounter portCounter : jobRecord.getInputCounters()) {
      if (portCounter.isScatter()) {
        result.add(portCounter.getPort());
      }
    }
    return result;
  }

  public void increaseInputPortIncoming(JobRecord jobRecord, String port) {
    for (PortCounter portCounter : jobRecord.getInputCounters()) {
      if (portCounter.getPort().equals(port)) {
        portCounter.increaseIncoming();
        return;
      }
    }
  }
  
  public void increaseOutputPortIncoming(JobRecord jobRecord, String port) {
    for (PortCounter portCounter : jobRecord.getOutputCounters()) {
      if (portCounter.getPort().equals(port)) {
        portCounter.increaseIncoming();;
        return;
      }
    }
  }
  
}
