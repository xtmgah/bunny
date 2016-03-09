package org.rabix.engine.model;

import java.util.ArrayList;
import java.util.List;

import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.model.scatter.ScatterMapping;
import org.rabix.engine.service.JobService.JobState;

public class JobRecord {

  private final String id;
  private final String externalId;
  private final String contextId;
  private final boolean master;
  
  private JobState state;
  
  private List<PortCounter> inputCounters;
  private List<PortCounter> outputCounters;

  private boolean isScattered;                  // it's created from scatter
  private boolean isContainer;                  // it's a container Job
  private boolean isScatterWrapper;             // it's a scatter wrapper

  private int numberOfGlobalInputs = 0;
  private int numberOfGlobalOutputs = 0;
  
  private ScatterMapping scatterMapping;
  
  public JobRecord(String contextId, String id, String uniqueId, JobState state, boolean isContainer, boolean fromScatter, boolean master) {
    this.id = id;
    this.externalId = uniqueId;
    this.contextId = contextId;
    this.state = state;
    this.master = master;
    this.isContainer = isContainer;
    this.isScattered = fromScatter;
    this.inputCounters = new ArrayList<>();
    this.outputCounters = new ArrayList<>();
  }
  
  public String getId() {
    return id;
  }
  
  public String getExternalId() {
    return externalId;
  }

  public String getContextId() {
    return contextId;
  }
  
  public boolean isMaster() {
    return master;
  }
  
  public JobState getState() {
    return state;
  }

  public void setState(JobState state) {
    this.state = state;
  }

  public List<PortCounter> getInputCounters() {
    return inputCounters;
  }

  public void setInputCounters(List<PortCounter> inputCounters) {
    this.inputCounters = inputCounters;
  }

  public List<PortCounter> getOutputCounters() {
    return outputCounters;
  }

  public void setOutputCounters(List<PortCounter> outputCounters) {
    this.outputCounters = outputCounters;
  }

  public boolean isContainer() {
    return isContainer;
  }

  public void setContainer(boolean isContainer) {
    this.isContainer = isContainer;
  }

  public boolean isScattered() {
    return isScattered;
  }

  public void setScattered(boolean isScattered) {
    this.isScattered = isScattered;
  }

  public boolean isScatterWrapper() {
    return isScatterWrapper;
  }

  public void setScatterWrapper(boolean isScatterWrapper) {
    this.isScatterWrapper = isScatterWrapper;
  }

  public ScatterMapping getScatterMapping() {
    return scatterMapping;
  }

  public void setScatterMapping(ScatterMapping scatterMapping) {
    this.scatterMapping = scatterMapping;
  }

  public boolean isInputPortReady(String port) {
    for (PortCounter pc : inputCounters) {
      if (pc.port.equals(port)) {
        if (pc.counter == 0) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isOutputPortReady(String port) {
    for (PortCounter pc : outputCounters) {
      if (pc.port.equals(port)) {
        if (pc.counter == 0) {
          return true;
        }
      }
    }
    return false;
  }

  public void incrementPortCounter(DAGLinkPort port, LinkPortType type) {
    List<PortCounter> counters = type.equals(LinkPortType.INPUT) ? inputCounters : outputCounters;

    for (PortCounter pc : counters) {
      if (pc.port.equals(port.getId())) {
        pc.counter = pc.counter + 1;
        return;
      }
    }
    PortCounter portCounter = new PortCounter(port.getId(), 1, port.isScatter());
    counters.add(portCounter);
  }
  
  public void incrementPortCounterIfThereIsNo(DAGLinkPort port, LinkPortType type) {
    List<PortCounter> counters = type.equals(LinkPortType.INPUT) ? inputCounters : outputCounters;

    for (PortCounter pc : counters) {
      if (pc.port.equals(port.getId()) && pc.counter < 1) {
        pc.counter = pc.counter + 1;
        return;
      }
    }
    PortCounter portCounter = new PortCounter(port.getId(), 1, port.isScatter());
    counters.add(portCounter);
  }
  
  public void decrementPortCounter(String portId, LinkPortType type) {
    List<PortCounter> counters = type.equals(LinkPortType.INPUT) ? inputCounters : outputCounters;
    for (PortCounter portCounter : counters) {
      if (portCounter.port.equals(portId)) {
        portCounter.counter = portCounter.counter - 1;
      }
    }
  }
  
  public void resetInputPortCounters(int value) {
    if (numberOfGlobalInputs == value) {
      return;
    }
    int oldValue = numberOfGlobalInputs;
    if (numberOfGlobalInputs < value) {
      numberOfGlobalInputs = value;

      for (PortCounter pc : inputCounters) {
        if (pc.counter != value) {
          if (pc.counter == 0) {
            continue;
          }
          if (oldValue != 0) {
            pc.counter = numberOfGlobalInputs - (oldValue - pc.counter);
          } else {
            pc.counter = numberOfGlobalInputs;
          }
        }
      }
    }
  }

  public void resetOutputPortCounters(int value) {
    if (numberOfGlobalOutputs == value) {
      return;
    }
    int oldValue = numberOfGlobalOutputs;
    if (numberOfGlobalOutputs < value) {
      numberOfGlobalOutputs = value;

      for (PortCounter pc : outputCounters) {
        if (pc.counter == 0) {
          continue;
        }
        if (pc.counter != value) {
          if (oldValue != 0) {
            pc.counter = numberOfGlobalOutputs - (oldValue - pc.counter);
          } else {
            pc.counter = numberOfGlobalOutputs;
          }
        }
      }
    }
  }

  public boolean isReady() {
    for (PortCounter portCounter : inputCounters) {
      if (portCounter.counter > 0) {
        return false;
      }
    }
    return true;
  }

  public boolean isCompleted() {
    for (PortCounter portCounter : outputCounters) {
      if (portCounter.counter > 0) {
        return false;
      }
    }
    return true;
  }

  public boolean isScatterPort(String port) {
    for (PortCounter portCounter : inputCounters) {
      if (portCounter.port.equals(port)) {
        return portCounter.scatter;
      }
    }
    return false;
  }

  public List<String> getScatterPorts() {
    List<String> result = new ArrayList<>();

    for (PortCounter portCounter : inputCounters) {
      if (portCounter.scatter) {
        result.add(portCounter.port);
      }
    }
    return result;
  }

  public Integer getNumberOfGlobalOutputs() {
    return numberOfGlobalOutputs;
  }

  public class PortCounter {
    private String port;
    private int counter;
    private boolean scatter;

    PortCounter(String port, int counter, boolean scatter) {
      this.port = port;
      this.counter = counter;
      this.scatter = scatter;
    }

    public String getPort() {
      return port;
    }

    public void setPort(String port) {
      this.port = port;
    }

    public int getCounter() {
      return counter;
    }

    public void setCounter(int counter) {
      this.counter = counter;
    }

    public boolean isScatter() {
      return scatter;
    }

    public void setScatter(boolean scatter) {
      this.scatter = scatter;
    }

  }

}
