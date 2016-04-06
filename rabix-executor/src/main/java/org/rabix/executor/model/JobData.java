package org.rabix.executor.model;

import java.util.Map;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;

public class JobData {

  private Job job;
  private JobStatus status;
  private Map<String, Object> result;
  private String message;
  private boolean important;
  private boolean terminal;
  private boolean logsUploaded;

  public JobData(Job job, JobStatus status, boolean important, boolean terminal) {
    this.job = job;
    this.status = status;
    this.important = important;
    this.terminal = terminal;
    this.logsUploaded = false;
  }

  public Job getJob() {
    return job;
  }

  public void setJob(Job job) {
    this.job = job;
  }

  public JobStatus getStatus() {
    return status;
  }

  public void setStatus(JobStatus status) {
    this.status = status;
  }

  public Map<String, Object> getResult() {
    return result;
  }

  public void setResult(Map<String, Object> result) {
    this.result = result;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isImportant() {
    return important;
  }

  public void setImportant(boolean important) {
    this.important = important;
  }

  public boolean isTerminal() {
    return terminal;
  }

  public void setTerminal(boolean terminal) {
    this.terminal = terminal;
  }

  public boolean isLogsUploaded() {
    return logsUploaded;
  }

  public void setLogsUploaded(boolean logsUploaded) {
    this.logsUploaded = logsUploaded;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((job == null) ? 0 : job.hashCode());
    result = prime * result + (important ? 1231 : 1237);
    result = prime * result + (logsUploaded ? 1231 : 1237);
    result = prime * result + ((message == null) ? 0 : message.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    result = prime * result + (terminal ? 1231 : 1237);
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
    JobData other = (JobData) obj;
    if (job == null) {
      if (other.job != null)
        return false;
    } else if (!job.equals(other.job))
      return false;
    if (important != other.important)
      return false;
    if (logsUploaded != other.logsUploaded)
      return false;
    if (message == null) {
      if (other.message != null)
        return false;
    } else if (!message.equals(other.message))
      return false;
    if (status != other.status)
      return false;
    if (terminal != other.terminal)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "JobData [jobId=" + job.getId() + ", status=" + status + ", message=" + message + ", important="  + important + ", terminal=" + terminal + ", logsUploaded=" + logsUploaded + "]";
  }
}
