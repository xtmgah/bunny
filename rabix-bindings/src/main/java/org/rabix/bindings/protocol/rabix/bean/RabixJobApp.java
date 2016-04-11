package org.rabix.bindings.protocol.rabix.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.rabix.bindings.model.Application;
import org.rabix.bindings.protocol.draft2.bean.Draft2InputPort;
import org.rabix.bindings.protocol.draft2.bean.Draft2OutputPort;

public class RabixJobApp implements Application {

  protected String id;
  protected String raw;
  protected String template;
  private List<RabixPort> inputs;
  private List<RabixPort> outputs;
  
  public RabixJobApp(String id, String raw, String template, Collection<String> inputs, Collection<String> outputs) {
    super();
    this.id = id;
    this.raw = raw;
    this.template = template;
    this.inputs = new ArrayList<RabixPort>();
    for(String input: inputs) {
      this.inputs.add(new RabixPort(input));
    }
    this.outputs = new ArrayList<RabixPort>();
    for(String output: outputs) {
      this.outputs.add(new RabixPort(output));
    }
  }

  public String getId() {
    return id;
  }

  public String getTemplate() {
    return template;
  }

  public RabixPort getPort(String id, Class<? extends RabixPort> clazz) {
    if (Draft2InputPort.class.equals(clazz)) {
      return getInput(id);
    }
    if (Draft2OutputPort.class.equals(clazz)) {
      return getOutput(id);
    }
    return null;
  }

  public RabixPort getInput(String id) {
    if (getInputs() == null) {
      return null;
    }
    for (RabixPort input : getInputs()) {
      if (input.getId().toString().equals(id) || input.getId().equals(id)) {
        return input;
      }
    }
    return null;
  }

  public RabixPort getOutput(String id) {
    if (getOutputs() == null) {
      return null;
    }
    for (RabixPort output : getOutputs()) {
      if (output.getId().toString().equals(id) || output.getId().equals(id)) {
        return output;
      }
    }
    return null;
  }

  public List<RabixPort> getInputs() {
    return inputs;
  }

  public List<RabixPort> getOutputs() {
    return outputs;
  }
  
  public String getRaw() {
    return raw;
  }

  @Override
  public String serialize() {
    return raw;
  }

}
