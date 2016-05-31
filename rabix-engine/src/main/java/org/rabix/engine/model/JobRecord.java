package org.rabix.engine.model;

import java.util.ArrayList;
import java.util.List;

import org.rabix.engine.model.scatter.ScatterStrategy;
import org.rabix.engine.service.JobRecordService.JobState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JobRecord {

  private String id;
  private String externalId;
  private String rootId;
  private String parentId;
  private boolean blocking;
  
  private JobState state;
  
  private List<PortCounter> inputCounters;
  private List<PortCounter> outputCounters;

  private boolean isScattered;                  // it's created from scatter
  private boolean isContainer;                  // it's a container Job
  private boolean isScatterWrapper;             // it's a scatter wrapper

  private int globalInputsCount = 0;
  private int globalOutputsCount = 0;
  
  private ScatterStrategy scatterStrategy;
  
  public JobRecord(String rootId, String id, String uniqueId, String parentId, JobState state, boolean isContainer, boolean isScattered, boolean blocking) {
    this.id = id;
    this.externalId = uniqueId;
    this.rootId = rootId;
    this.parentId = parentId;
    this.state = state;
    this.blocking = blocking;
    this.isContainer = isContainer;
    this.isScattered = isScattered;
    this.inputCounters = new ArrayList<>();
    this.outputCounters = new ArrayList<>();
  }
  
  public boolean isRoot() {
    return externalId.equals(rootId);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public String getRootId() {
    return rootId;
  }

  public void setRootId(String rootId) {
    this.rootId = rootId;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public boolean isBlocking() {
    return blocking;
  }

  public void setBlocking(boolean blocking) {
    this.blocking = blocking;
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

  public boolean isScattered() {
    return isScattered;
  }

  public void setScattered(boolean isScattered) {
    this.isScattered = isScattered;
  }

  public boolean isContainer() {
    return isContainer;
  }

  public void setContainer(boolean isContainer) {
    this.isContainer = isContainer;
  }

  public boolean isScatterWrapper() {
    return isScatterWrapper;
  }

  public void setScatterWrapper(boolean isScatterWrapper) {
    this.isScatterWrapper = isScatterWrapper;
  }

  public int getGlobalInputsCount() {
    return globalInputsCount;
  }

  public void setGlobalInputsCount(int globalInputsCount) {
    this.globalInputsCount = globalInputsCount;
  }

  public int getGlobalOutputsCount() {
    return globalOutputsCount;
  }

  public void setGlobalOutputsCount(int globalOutputsCount) {
    this.globalOutputsCount = globalOutputsCount;
  }

  public ScatterStrategy getScatterStrategy() {
    return scatterStrategy;
  }

  public void setScatterStrategy(ScatterStrategy scatterStrategy) {
    this.scatterStrategy = scatterStrategy;
  }

  public static class PortCounter {
    @JsonProperty("port")
    private String port;
    @JsonProperty("counter")
    private int counter;
    @JsonProperty("scatter")
    private boolean scatter;
    @JsonProperty("incoming")
    private int incoming;

    @JsonCreator
    public PortCounter(@JsonProperty("port") String port, @JsonProperty("counter") int counter,
        @JsonProperty("scatter") boolean scatter, @JsonProperty("incoming") int incoming) {
      this.port = port;
      this.counter = counter;
      this.scatter = scatter;
      this.incoming = incoming;
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

    public int getIncoming() {
      return incoming;
    }

    public void setIncoming(int incoming) {
      this.incoming = incoming;
    }

    @Override
    public String toString() {
      return "PortCounter [port=" + port + ", counter=" + counter + ", scatter=" + scatter + ", incoming=" + incoming + "]";
    }

  }

  @Override
  public String toString() {
    return "JobRecord [id=" + id + ", externalId=" + externalId + ", rootId=" + rootId + ", parentId=" + parentId + ", blocking=" + blocking + ", state=" + state + ", inputCounters=" + inputCounters + ", outputCounters=" + outputCounters + ", isScattered=" + isScattered + ", isContainer=" + isContainer + ", isScatterWrapper=" + isScatterWrapper + ", globalInputsCount=" + globalInputsCount + ", globalOutputsCount=" + globalOutputsCount + ", scatterStrategy=" + scatterStrategy + "]";
  }

}
