package org.rabix.bindings.protocol.draft2.bean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.protocol.draft2.expression.Draft2ExpressionException;
import org.rabix.bindings.protocol.draft2.expression.helper.Draft2ExpressionBeanHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Draft2CommandLineTool extends Draft2JobApp {

  public static final String KEY_ARGUMENT_VALUE = "valueFrom";

  @JsonProperty("stdin")
  private Object stdin;
  @JsonProperty("stdout")
  private Object stdout;
  @JsonProperty("baseCommand")
  private List<Object> baseCommands;
  @JsonProperty("arguments")
  private List<Object> arguments;
  

  public Draft2CommandLineTool() {
    super();
    this.baseCommands = new ArrayList<>();
    this.arguments = new ArrayList<>();
  }

  public List<Object> getBaseCmd(Draft2Job job) throws Draft2ExpressionException {
    List<Object> result = new LinkedList<>();

    for (Object baseCmd : baseCommands) {
      if (Draft2ExpressionBeanHelper.isExpression(baseCmd)) {
        Object transformed = Draft2ExpressionBeanHelper.evaluate(job, baseCmd);
        if (transformed != null) {
          result.add(transformed);
        }
      } else {
        result.add(baseCmd);
      }
    }
    return result;
  }

  public String getStdin(Draft2Job job) throws Draft2ExpressionException {
    if (Draft2ExpressionBeanHelper.isExpression(stdin)) {
      return Draft2ExpressionBeanHelper.evaluate(job, stdin);
    }
    return stdin != null ? stdin.toString() : "";
  }

  public String getStdout(Draft2Job job) throws Draft2ExpressionException {
    if (Draft2ExpressionBeanHelper.isExpression(stdout)) {
      return Draft2ExpressionBeanHelper.evaluate(job, stdout);
    }
    return stdout != null ? stdout.toString() : "";
  }

  public String getStderr(Draft2Job job) throws Draft2ExpressionException {
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
  public Object getArgument(Draft2Job job, Object binding) throws Draft2ExpressionException {
    if (binding instanceof Map<?, ?>) {
      Object value = ((Map<?, ?>) binding).get(KEY_ARGUMENT_VALUE);
      if (value == null) {
        return null;
      }
      if (Draft2ExpressionBeanHelper.isExpression(value)) {
        return Draft2ExpressionBeanHelper.evaluate(job, value);
      }
      return value;
    }
    return null;
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
  public Draft2JobAppType getType() {
    return Draft2JobAppType.COMMAND_LINE_TOOL;
  }

  @Override
  public String toString() {
    return "CommandLineTool [stdin=" + stdin + ", stdout=" + stdout + ", baseCommands=" + baseCommands + ", arguments="
        + arguments + ", successCodes=" + successCodes + ", id=" + id + ", context=" + context + ", description="
        + description + ", label=" + label + ", contributor=" + contributor + ", owner=" + owner + ", inputs=" + getInputs()
        + ", outputs=" + getOutputs() + ", requirements=" + requirements + "]";
  }

}
