package org.rabix.bindings.model;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.EncodingHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Executable {

  public static enum ExecutableStatus {
    READY,
    STARTED,
    STOPPED,
    FAILED,
    FINISHED
  }

  @JsonProperty("id")
  private final String id;
  @JsonProperty("nodeId")
  private final String nodeId;
  @JsonProperty("app")
  private final String app;
  @JsonProperty("status")
  private final ExecutableStatus status;
  @JsonProperty("context")
  private final Context context;
  @JsonProperty("inputs")
  private final Object inputs;
  @JsonProperty("outputs")
  private final Object outputs;
  @JsonProperty("allocatedResources")
  private final Resources allocatedResources;
  
  @JsonProperty("processed")
  private final boolean processed;

  public Executable(String id, String nodeId, DAGNode node, ExecutableStatus status, Object inputs, Resources allocatedResources, Context context) {
    this.id = id;
    this.nodeId = nodeId;
    this.status = status;
    this.inputs = inputs;
    this.outputs = null;
    this.context = context;
    this.processed = false;
    this.allocatedResources = allocatedResources;
    this.app = EncodingHelper.encodeBase64(node.getApp());
  }

  @JsonCreator
  public Executable(@JsonProperty("id") String id, 
      @JsonProperty("processed") boolean processed,
      @JsonProperty("nodeId") String nodeId,
      @JsonProperty("app") String app, 
      @JsonProperty("status") ExecutableStatus status, 
      @JsonProperty("allocatedResources") Resources allocatedResources,
      @JsonProperty("inputs") Object inputs, 
      @JsonProperty("outputs") Object otputs,
      @JsonProperty("context") Context context) {
    this.id = id;
    this.nodeId = nodeId;
    this.app = app;
    this.status = status;
    this.inputs = inputs;
    this.outputs = otputs;
    this.context = context;
    this.processed = processed;
    this.allocatedResources = allocatedResources;
  }

  public static Executable cloneWithResources(Executable executable, Resources resources) {
    return new Executable(executable.id, executable.processed, executable.nodeId, executable.app, executable.status, resources, executable.inputs, executable.outputs, executable.context);
  }

  public static Executable cloneWithStatus(Executable executable, ExecutableStatus status) {
    return new Executable(executable.id, executable.processed, executable.nodeId, executable.app, status, executable.allocatedResources, executable.inputs, executable.outputs, executable.context);
  }
  
  public static Executable cloneWithInputs(Executable executable, Object inputs) {
    return new Executable(executable.id, executable.processed, executable.nodeId, executable.app, executable.status, executable.allocatedResources, inputs, executable.outputs, executable.context);
  }
  
  public static Executable cloneWithOutputs(Executable executable, Object outputs) {
    return new Executable(executable.id, executable.processed, executable.nodeId, executable.app, executable.status, executable.allocatedResources, executable.inputs, outputs, executable.context);
  }
  
  public static Executable cloneWithProcessed(Executable executable, boolean processed) {
    return new Executable(executable.id, processed, executable.nodeId, executable.app, executable.status, executable.allocatedResources, executable.inputs, executable.outputs, executable.context);
  }
  
  public String getId() {
    return id;
  }
  
  public boolean isProcessed() {
    return processed;
  }
  
  public String getNodeId() {
    return nodeId;
  }
  
  @JsonIgnore
  public <T> T getApp(Class<T> clazz) {
    return EncodingHelper.decodeBase64(app, clazz);
  }
  
  public Resources getAllocatedResources() {
    return allocatedResources;
  }
  
  public Object getInputs() {
    return inputs;
  }
  
  @JsonIgnore
  public <T> T getInputs(Class<T> clazz) throws BindingException {
    if (inputs == null) {
      return null;
    }
    if (clazz.isInstance(inputs)) {
      return clazz.cast(inputs);
    }
    throw new BindingException("Invalid Executable inputs section. Inputs: " + inputs);
  }
  
  public Object getOutputs() {
    return outputs;
  }
  
  @JsonIgnore
  public <T> T getOutputs(Class<T> clazz) throws BindingException {
    if (outputs == null) {
      return null;
    }
    if (clazz.isInstance(outputs)) {
      return clazz.cast(outputs);
    }
    throw new BindingException("Invalid Executable outputs section. Outputs: " + inputs);
  }
  
  public ExecutableStatus getStatus() {
    return status;
  }
  
  public Context getContext() {
    return context;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((allocatedResources == null) ? 0 : allocatedResources.hashCode());
    result = prime * result + ((app == null) ? 0 : app.hashCode());
    result = prime * result + ((context == null) ? 0 : context.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
    result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
    result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Executable other = (Executable) obj;
    if (allocatedResources == null) {
      if (other.allocatedResources != null)
        return false;
    } else if (!allocatedResources.equals(other.allocatedResources))
      return false;
    if (app == null) {
      if (other.app != null)
        return false;
    } else if (!app.equals(other.app))
      return false;
    if (context == null) {
      if (other.context != null)
        return false;
    } else if (!context.equals(other.context))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (inputs == null) {
      if (other.inputs != null)
        return false;
    } else if (!inputs.equals(other.inputs))
      return false;
    if (nodeId == null) {
      if (other.nodeId != null)
        return false;
    } else if (!nodeId.equals(other.nodeId))
      return false;
    if (outputs == null) {
      if (other.outputs != null)
        return false;
    } else if (!outputs.equals(other.outputs))
      return false;
    if (status != other.status)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Executable [id=" + id + ", nodeId=" + nodeId + ", status=" + status + ", context=" + context + ", allocatedResources=" + allocatedResources + ", processed=" + processed + "]";
  }

}
