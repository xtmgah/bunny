package org.rabix.bindings.protocol.draft4.bean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.protocol.draft4.expression.Draft4ExpressionException;
import org.rabix.bindings.protocol.draft4.expression.Draft4ExpressionResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Draft4CommandLineTool extends Draft4JobApp {

  public static final String KEY_ARGUMENT_VALUE = "valueFrom";

  @JsonProperty("stdin")
  private Object stdin;
  @JsonProperty("stdout")
  private Object stdout;
  @JsonProperty("baseCommand")
  private Object baseCommand;
  @JsonProperty("arguments")
  private List<Object> arguments;
  

  public Draft4CommandLineTool() {
    super();
    this.baseCommand = new ArrayList<>();
    this.arguments = new ArrayList<>();
  }

  @SuppressWarnings("unchecked")
  public List<Object> getBaseCmd(Draft4Job job) throws Draft4ExpressionException {
    List<Object> result = new LinkedList<>();
    if (baseCommand instanceof List<?>) {
      result = (List<Object>) baseCommand;
    } else if (baseCommand instanceof String) {
      result = new LinkedList<>();
      result.add(baseCommand);
    }
    return result;
  }
  
  public String getStdin(Draft4Job job) throws Draft4ExpressionException {
    String evaluatedStdin = Draft4ExpressionResolver.resolve(stdin, job, null);
    return evaluatedStdin != null ? evaluatedStdin.toString() : "";
  }

  public String getStdout(Draft4Job job) throws Draft4ExpressionException {
    String evaluatedStdout = Draft4ExpressionResolver.resolve(stdout, job, null);
    return evaluatedStdout != null ? evaluatedStdout.toString() : "";
  }

  public String getStderr(Draft4Job job) throws Draft4ExpressionException {
    String stdout = getStdout(job);
    return changeExtension(stdout, "err");
  }

  @JsonIgnore
  public boolean hasArguments() {
    return arguments != null;
  }

  public List<Object> getArguments() {
    return arguments;
  }

  @JsonIgnore
  public Object getArgument(Draft4Job job, Object binding) throws Draft4ExpressionException {
    if (binding instanceof Map<?, ?>) {
      Object value = ((Map<?, ?>) binding).get(KEY_ARGUMENT_VALUE);
      if (value != null) {
        return Draft4ExpressionResolver.resolve(value, job, null);
      }
    }
    return Draft4ExpressionResolver.resolve(binding, job, null);
  }

  /**
   * Replaces extension if there is any
   */
  private String changeExtension(String fileName, String extension) {
    if (fileName == null) {
      return null;
    }
    int lastIndexOf = fileName.lastIndexOf(".");
    return lastIndexOf != -1 ? fileName.substring(0, lastIndexOf + 1) + extension : fileName + "." + extension;
  }

  @Override
  @JsonIgnore
  public Draft4JobAppType getType() {
    return Draft4JobAppType.COMMAND_LINE_TOOL;
  }

  @Override
  public String toString() {
    return "CommandLineTool [stdin=" + stdin + ", stdout=" + stdout + ", baseCommands=" + baseCommand + ", arguments="
        + arguments + ", successCodes=" + successCodes + ", id=" + id + ", context=" + context + ", description="
        + description + ", label=" + label + ", contributor=" + contributor + ", owner=" + owner + ", inputs=" + getInputs()
        + ", outputs=" + getOutputs() + ", requirements=" + requirements + "]";
  }

}
