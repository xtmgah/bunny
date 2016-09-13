package org.rabix.bindings.cwl1.bean;

import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class CWL1EmbeddedApp extends CWL1JobApp {

  private Application application;
  private List<CWL1InputPort> inputs;
  private List<CWL1OutputPort> outputs;

  @JsonCreator
  public CWL1EmbeddedApp(String raw) {
    try {
      Bindings bindings = BindingsFactory.create(raw);

      application = bindings.loadAppObject(raw);
      inputs = Lists.transform(application.getInputs(), new Function<ApplicationPort, CWL1InputPort>() {
        @Override
        public CWL1InputPort apply(ApplicationPort port) {
          return new CWL1InputPort(port.getId(), port.getDefaultValue(), port.getSchema(), null, null, null,
              port.getScatter(), null, port.getLinkMerge());
        }
      });
      outputs = Lists.transform(application.getOutputs(), new Function<ApplicationPort, CWL1OutputPort>() {
        @Override
        public CWL1OutputPort apply(ApplicationPort port) {
          return new CWL1OutputPort(port.getId(), port.getDefaultValue(), port.getSchema(), null, port.getScatter(),
              null, port.getLinkMerge());
        }
      });
    } catch (BindingException e1) {
      // TOOD implement
    }
  }

  @Override
  public List<CWL1InputPort> getInputs() {
    return inputs;
  }

  @Override
  public List<CWL1OutputPort> getOutputs() {
    return outputs;
  }

  @Override
  public String serialize() {
    return application.serialize();
  }

  @Override
  public CWL1JobAppType getType() {
    return CWL1JobAppType.EMBEDDED;
  }

}
