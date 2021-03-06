package org.rabix.bindings.sb.bean;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SBTask {

  @JsonProperty("job")
  private SBJob job;
  @JsonProperty("external_task_id")
  private String externalTaskId;
  @JsonProperty("charging_context")
  private String chargingContext;
  @JsonProperty("project_id")
  private String projectId;
  @JsonProperty("project_slug")
  private String projectSlug;
  @JsonProperty("instance_security")
  private Map<String, Object> instanceSecurity;

  public SBTask() {
  }

  public SBJob getJob() {
    return job;
  }

  public void setJob(SBJob job) {
    this.job = job;
  }

  public String getExternalTaskId() {
    return externalTaskId;
  }

  public void setExternalTaskId(String externalTaskId) {
    this.externalTaskId = externalTaskId;
  }

  public String getChargingContext() {
    return chargingContext;
  }

  public void setChargingContext(String chargingContext) {
    this.chargingContext = chargingContext;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getProjectSlug() {
    return projectSlug;
  }

  public void setProjectSlug(String projectSlug) {
    this.projectSlug = projectSlug;
  }

  public Map<String, Object> getInstanceSecurity() {
    return instanceSecurity;
  }

  public void setInstanceSecurity(Map<String, Object> instanceSecurity) {
    this.instanceSecurity = instanceSecurity;
  }

  @Override
  public String toString() {
    return "Task [job=" + job + ", externalTaskId=" + externalTaskId + ", chargingContext=" + chargingContext
        + ", projectId=" + projectId + ", projectSlug=" + projectSlug + ", instanceSecurity=" + instanceSecurity + "]";
  }

}
