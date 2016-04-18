package org.rabix.bindings.model;

import java.util.Map;
import java.util.UUID;

import org.rabix.common.helper.CloneHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
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
  @JsonProperty("parentId")
  private final String parentId;
  @JsonProperty("rootId")
  private final String rootId;
  @JsonProperty("name")
  private final String name;
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
  
  public Job(String app, Map<String, Object> inputs) {
    this(null, null, UUID.randomUUID().toString(), null, app, null, inputs, null, null);
  }
  
  @JsonCreator
  public Job(@JsonProperty("id") String id,
      @JsonProperty("parentId") String parentId,
      @JsonProperty("rootId") String rootId,
      @JsonProperty("name") String name,
      @JsonProperty("app") String app, 
      @JsonProperty("status") JobStatus status, 
      @JsonProperty("inputs") Map<String, Object> inputs, 
      @JsonProperty("outputs") Map<String, Object> otputs,
      @JsonProperty("context") Context context) {
    this.id = id;
    this.parentId = parentId;
    this.rootId = rootId;
    this.name = name;
    this.app = app;
    this.status = status;
    this.inputs = inputs;
    this.outputs = otputs;
    this.context = context;
  }
  
  public static Job cloneWithId(Job job, String id) {
    return new Job(id, job.parentId, job.rootId, job.name, job.app, job.status, job.inputs, job.outputs, job.context);
  }
  
  public static Job cloneWithContext(Job job, Context context) {
    return new Job(job.id, job.parentId, job.rootId, job.name, job.app, job.status, job.inputs, job.outputs, context);
  }
  
  public static Job cloneWithResources(Job job, Resources resources) {
    return new Job(job.id, job.parentId, job.rootId, job.name, job.app, job.status, job.inputs, job.outputs, job.context);
  }

  public static Job cloneWithStatus(Job job, JobStatus status) {
    return new Job(job.id, job.parentId, job.rootId, job.name, job.app, status, job.inputs, job.outputs, job.context);
  }
  
  public static Job cloneWithInputs(Job job, Map<String, Object> inputs) {
    return new Job(job.id, job.parentId, job.rootId, job.name, job.app, job.status, inputs, job.outputs, job.context);
  }
  
  public static Job cloneWithOutputs(Job job, Map<String, Object> outputs) {
    return new Job(job.id, job.parentId, job.rootId, job.name, job.app, job.status, job.inputs, outputs, job.context);
  }
  
  public static boolean isFinished(Job job) {
    return job.getStatus().equals(JobStatus.COMPLETED) 
        || job.getStatus().equals(JobStatus.ABORTED)
        || job.getStatus().equals(JobStatus.FAILED);
  }
  
  public String getId() {
    return id;
  }
  
  public String getParentId() {
    return parentId;
  }
  
  public String getRootId() {
    return rootId;
  }
  
  public String getName() {
    return name;
  }
  
  public String getApp() {
    return app;
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
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Job [id=" + id + ", parentId=" + parentId + ", rootId=" + rootId + ", name=" + name + ", status=" + status + ", context=" + context + ", inputs=" + inputs + ", outputs=" + outputs + "]";
  }
}
