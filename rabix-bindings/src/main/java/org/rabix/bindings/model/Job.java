package org.rabix.bindings.model;

import java.io.IOException;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.common.helper.CloneHelper;
import org.rabix.common.json.BeanSerializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Job {

  public static enum JobStatus {
    PENDING,
    READY,
    STARTED,
    ABORTED,
    FAILED,
    COMPLETED,
    RUNNING
  }

  @JsonProperty("id")
  private final String id;
  @JsonProperty("nodeId")
  private final String nodeId;
  @JsonProperty("app")
  private final String app;
  @JsonProperty("status")
  private final JobStatus status;
  @JsonProperty("context")
  private final Context context;
  @JsonProperty("inputs")
  private final Map<String, Object> inputs;
  @JsonProperty("outputs")
  private final Map<String, Object> outputs;
  
  @JsonCreator
  public Job(@JsonProperty("id") String id, 
      @JsonProperty("nodeId") String nodeId,
      @JsonProperty("app") String app, 
      @JsonProperty("status") JobStatus status, 
      @JsonProperty("inputs") Map<String, Object> inputs, 
      @JsonProperty("outputs") Map<String, Object> otputs,
      @JsonProperty("context") Context context) {
    this.id = id;
    this.nodeId = nodeId;
    this.app = app;
    this.status = status;
    this.inputs = inputs;
    this.outputs = otputs;
    this.context = context;
  }

  public static Job cloneWithResources(Job job, Resources resources) {
    return new Job(job.id, job.nodeId, job.app, job.status, job.inputs, job.outputs, job.context);
  }

  public static Job cloneWithStatus(Job job, JobStatus status) {
    return new Job(job.id, job.nodeId, job.app, status, job.inputs, job.outputs, job.context);
  }
  
  public static Job cloneWithInputs(Job job, Map<String, Object> inputs) {
    return new Job(job.id, job.nodeId, job.app, job.status, inputs, job.outputs, job.context);
  }
  
  public static Job cloneWithOutputs(Job job, Map<String, Object> outputs) {
    return new Job(job.id, job.nodeId, job.app, job.status, job.inputs, outputs, job.context);
  }
  
  public String getId() {
    return id;
  }
  
  public String getNodeId() {
    return nodeId;
  }
  
  @JsonIgnore
  public <T> T getApp(Class<T> clazz) throws BindingException {
    try {
      String decodedApp = URIHelper.getData(app);
      return BeanSerializer.deserialize(decodedApp, clazz);
    } catch (IOException e) {
      throw new BindingException("Failed to get applicaton object", e);
    }
  }
  
  @SuppressWarnings("unchecked")
  public Map<String, Object> getInputs() {
    try {
      return (Map<String, Object>) CloneHelper.deepCopy(inputs);
    } catch (Exception e) {
      throw new RuntimeException("Failed to clone inputs", e);
    }
  }
  
  @SuppressWarnings("unchecked")
  public Map<String, Object> getOutputs() {
    try {
      return (Map<String, Object>) CloneHelper.deepCopy(outputs);
    } catch (Exception e) {
      throw new RuntimeException("Failed to clone outputs", e);
    }
  }
  
  public JobStatus getStatus() {
    return status;
  }
  
  public Context getContext() {
    return context;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((context == null) ? 0 : context.getId().hashCode());
    result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
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
    Job other = (Job) obj;
    if (context == null) {
      if (other.context != null)
        return false;
    } else if (!context.getId().equals(other.context.getId()))
      return false;
    if (nodeId == null) {
      if (other.nodeId != null)
        return false;
    } else if (!nodeId.equals(other.nodeId))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Job [id=" + id + ", nodeId=" + nodeId + ", status=" + status + ", context=" + context + ", inputs=" + inputs + ", outputs=" + outputs + "]";
  }
}
