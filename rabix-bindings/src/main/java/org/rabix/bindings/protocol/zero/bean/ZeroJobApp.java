package org.rabix.bindings.protocol.zero.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.rabix.bindings.model.Application;
import org.rabix.bindings.protocol.draft2.bean.Draft2InputPort;
import org.rabix.bindings.protocol.draft2.bean.Draft2OutputPort;

public class ZeroJobApp implements Application {

  protected String id;
  protected String raw;
  protected String template;
  
  private List<ZeroPort> inputs;
  private List<ZeroPort> outputs;
  
  public ZeroJobApp(String id, String raw, String template, Collection<String> inputs, Collection<String> outputs) {
    super();
    this.id = id;
    this.raw = raw;
    this.template = template;
    this.inputs = new ArrayList<ZeroPort>();
    for(String input: inputs) {
      this.inputs.add(new ZeroPort(input));
    }
    this.outputs = new ArrayList<ZeroPort>();
    for(String output: outputs) {
      this.outputs.add(new ZeroPort(output));
    }
  }

  public String getId() {
    return id;
  }

  public String getTemplate() {
    return template;
  }

  public ZeroPort getPort(String id, Class<? extends ZeroPort> clazz) {
    if (Draft2InputPort.class.equals(clazz)) {
      return getInput(id);
    }
    if (Draft2OutputPort.class.equals(clazz)) {
      return getOutput(id);
    }
    return null;
  }

  public ZeroPort getInput(String id) {
    if (getInputs() == null) {
      return null;
    }
    for (ZeroPort input : getInputs()) {
      if (input.getId().toString().equals(id) || input.getId().equals(id)) {
        return input;
      }
    }
    return null;
  }

  public ZeroPort getOutput(String id) {
    if (getOutputs() == null) {
      return null;
    }
    for (ZeroPort output : getOutputs()) {
      if (output.getId().toString().equals(id) || output.getId().equals(id)) {
        return output;
      }
    }
    return null;
  }

  public List<ZeroPort> getInputs() {
    return inputs;
  }

  public List<ZeroPort> getOutputs() {
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
