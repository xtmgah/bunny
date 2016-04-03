package org.rabix.bindings.protocol.draft2;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolAppProcessor;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.draft2.bean.Draft2InputPort;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.bean.Draft2JobApp;
import org.rabix.bindings.protocol.draft2.bean.Draft2JobAppType;
import org.rabix.bindings.protocol.draft2.helper.Draft2JobHelper;
import org.rabix.bindings.protocol.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.protocol.draft2.resolver.Draft2DocumentResolver;
import org.rabix.common.json.BeanSerializer;

public class Draft2AppProcessor implements ProtocolAppProcessor {

  private final Draft2DocumentResolver documentResolver;
  
  public Draft2AppProcessor() {
    documentResolver = new Draft2DocumentResolver();
  }
  
  @Override
  public String loadApp(String uri) throws BindingException {
    return documentResolver.resolve(uri);
  }
  
  @Override
  public Object loadAppObject(String app) throws BindingException {
    return BeanSerializer.deserialize(loadApp(app), Draft2JobApp.class);
  }

  @Override
  public boolean isSelfExecutable(Job job) throws BindingException {
    Draft2JobApp app = (Draft2JobApp) loadAppObject(job.getApp());
    return app.getType().equals(Draft2JobAppType.EXPRESSION_TOOL);
  }

  @Override
  public void validate(Job job) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);

    boolean throwException = false;
    StringBuilder builder = new StringBuilder("Missing inputs: ");

    Draft2JobApp draft2JobApp = draft2Job.getApp();
    for (Draft2InputPort inputPort : draft2JobApp.getInputs()) {
      if (Draft2SchemaHelper.isRequired(inputPort.getSchema())) {
        String inputPortId = Draft2SchemaHelper.normalizeId(inputPort.getId());
        if (!draft2Job.getInputs().containsKey(inputPortId)) {
          builder.append(throwException ? "," : "").append(inputPortId);
          throwException = true;
        }
      }
    }
    if (throwException) {
      throw new BindingException(builder.toString());
    }
  }
}
