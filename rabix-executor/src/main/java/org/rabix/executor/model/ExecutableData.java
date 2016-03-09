package org.rabix.executor.model;

import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.Executable.ExecutableStatus;

public class ExecutableData {

  private Executable executable;
  private ExecutableStatus status;
  private Object result;
  private String message;
  private boolean important;
  private boolean terminal;
  private boolean logsUploaded;

  public ExecutableData(Executable executable, ExecutableStatus status, boolean important, boolean terminal) {
    this.executable = executable;
    this.status = status;
    this.important = important;
    this.terminal = terminal;
    this.logsUploaded = false;
  }

  public Executable getExecutable() {
    return executable;
  }

  public void setExecutable(Executable executable) {
    this.executable = executable;
  }

  public ExecutableStatus getStatus() {
    return status;
  }

  public void setStatus(ExecutableStatus status) {
    this.status = status;
  }

  public Object getResult() {
    return result;
  }

  public void setResult(Object result) {
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
    result = prime * result + ((executable == null) ? 0 : executable.hashCode());
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
    ExecutableData other = (ExecutableData) obj;
    if (executable == null) {
      if (other.executable != null)
        return false;
    } else if (!executable.equals(other.executable))
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
    return "ExecutableData [executable=" + executable + ", status=" + status + ", message=" + message + ", important="  + important + ", terminal=" + terminal + ", logsUploaded=" + logsUploaded + "]";
  }
}
