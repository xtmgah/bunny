package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.db.DBException;
import org.rabix.engine.db.JobRecordRepository;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.JobRecord.PortCounter;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class JobRecordService {

  public static enum JobState {
    PENDING,
    READY,
    RUNNING,
    COMPLETED,
    FAILED
  }

  private final JobRecordRepository jobRecordRepository;
  
  @Inject
  public JobRecordService(final JobRecordRepository jobRecordRepository) {
    this.jobRecordRepository = jobRecordRepository;
  }
  
  public static String generateUniqueId() {
    return UUID.randomUUID().toString();
  }
  
  @Transactional
  public void create(JobRecord jobRecord) {
    try {
      jobRecordRepository.insert(jobRecord);
    } catch (DBException e) {
      // TODO handle
      e.printStackTrace();
    }
  }

  @Transactional
  public void update(JobRecord jobRecord) {
    try {
      jobRecordRepository.update(jobRecord);
    } catch (DBException e) {
      // TODO handle
      e.printStackTrace();
    }
  }
  
  @Transactional
  public List<JobRecord> findReady(String contextId) {
    try {
      return jobRecordRepository.findReady(contextId);
    } catch (DBException e) {
      // TODO handle
      e.printStackTrace();
    }
    return null;
  }
  
  @Transactional
  public JobRecord find(String id, String contextId) {
    try {
      return jobRecordRepository.find(id, contextId);
    } catch (DBException e) {
      // TODO handle
      e.printStackTrace();
    }
    return null;
  }
  
  @Transactional
  public JobRecord findRoot(String contextId) {
    try {
      return jobRecordRepository.findRoot(contextId);
    } catch (DBException e) {
      // TODO handle
      e.printStackTrace();
    }
    return null;
  }
  
  @Transactional
  public List<JobRecord> find(String contextId) {
    try {
      return jobRecordRepository.find(contextId);
    } catch (DBException e) {
      // TODO handle
      e.printStackTrace();
    }
    return null;
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
    PortCounter portCounter = new PortCounter(port.getId(), 1, port.isScatter(), 0);
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
      PortCounter portCounter = new PortCounter(port.getId(), 1, port.isScatter(), 0);
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
    if (jobRecord.getGlobalInputsCount() == value) {
      return;
    }
    int oldValue = jobRecord.getGlobalInputsCount();
    if (jobRecord.getGlobalInputsCount() < value) {
      jobRecord.setGlobalInputsCount(value);

      for (PortCounter pc : jobRecord.getInputCounters()) {
        if (pc.getCounter() != value) {
          if (pc.getCounter() == 0) {
            continue;
          }
          if (oldValue != 0) {
            pc.setCounter(jobRecord.getGlobalInputsCount() - (oldValue - pc.getCounter()));
          } else {
            pc.setCounter(jobRecord.getGlobalInputsCount());
          }
        }
      }
    }
  }

  public void resetOutputPortCounters(JobRecord jobRecord, int value) {
    if (jobRecord.getGlobalOutputsCount() == value) {
      return;
    }
    int oldValue = jobRecord.getGlobalOutputsCount();
    if (jobRecord.getGlobalOutputsCount() < value) {
      jobRecord.setGlobalOutputsCount(value);

      for (PortCounter pc : jobRecord.getOutputCounters()) {
        if (pc.getCounter() == 0) {
          continue;
        }
        if (pc.getCounter() != value) {
          if (oldValue != 0) {
            pc.setCounter(jobRecord.getGlobalOutputsCount() - (oldValue - pc.getCounter()));
          } else {
            pc.setCounter(jobRecord.getGlobalOutputsCount());
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
        portCounter.setIncoming(portCounter.getIncoming() + 1);
        return;
      }
    }
  }
  
  public void increaseOutputPortIncoming(JobRecord jobRecord, String port) {
    for (PortCounter portCounter : jobRecord.getOutputCounters()) {
      if (portCounter.getPort().equals(port)) {
        portCounter.setIncoming(portCounter.getIncoming() + 1);
        return;
      }
    }
  }
  
}
