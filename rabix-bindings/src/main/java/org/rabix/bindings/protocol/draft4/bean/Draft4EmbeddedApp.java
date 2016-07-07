package org.rabix.bindings.protocol.draft4.bean;

import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class Draft4EmbeddedApp extends Draft4JobApp {

  private Application application;
  private List<Draft4InputPort> inputs;
  private List<Draft4OutputPort> outputs;

  @JsonCreator
  public Draft4EmbeddedApp(String raw) {
    try {
      Bindings bindings = BindingsFactory.create(raw);

      application = bindings.loadAppObject(raw);
      inputs = Lists.transform(application.getInputs(), new Function<ApplicationPort, Draft4InputPort>() {
        @Override
        public Draft4InputPort apply(ApplicationPort port) {
          return new Draft4InputPort(port.getId(), port.getDefaultValue(), port.getSchema(), null, null, null,
              port.getScatter(), null, port.getLinkMerge());
        }
      });
      outputs = Lists.transform(application.getOutputs(), new Function<ApplicationPort, Draft4OutputPort>() {
        @Override
        public Draft4OutputPort apply(ApplicationPort port) {
          return new Draft4OutputPort(port.getId(), port.getDefaultValue(), port.getSchema(), null, port.getScatter(),
              null, port.getLinkMerge());
        }
      });
    } catch (BindingException e1) {
      // TOOD implement
    }
  }

  @Override
  public List<Draft4InputPort> getInputs() {
    return inputs;
  }

  @Override
  public List<Draft4OutputPort> getOutputs() {
    return outputs;
  }

  @Override
  public String serialize() {
    return application.serialize();
  }

  @Override
  public Draft4JobAppType getType() {
    return Draft4JobAppType.EMBEDDED;
  }

}
