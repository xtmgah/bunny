package org.rabix.bindings.protocol.draft4;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolAppProcessor;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.draft4.bean.Draft4InputPort;
import org.rabix.bindings.protocol.draft4.bean.Draft4Job;
import org.rabix.bindings.protocol.draft4.bean.Draft4JobApp;
import org.rabix.bindings.protocol.draft4.bean.Draft4JobAppType;
import org.rabix.bindings.protocol.draft4.helper.Draft4JobHelper;
import org.rabix.bindings.protocol.draft4.helper.Draft4SchemaHelper;
import org.rabix.bindings.protocol.draft4.resolver.Draft4DocumentResolver;
import org.rabix.common.json.BeanSerializer;

public class Draft4AppProcessor implements ProtocolAppProcessor {

  @Override
  public String loadApp(String uri) throws BindingException {
    return Draft4DocumentResolver.resolve(uri);
  }
  
  @Override
  public Application loadAppObject(String app) throws BindingException {
    return BeanSerializer.deserialize(loadApp(app), Draft4JobApp.class);
  }

  @Override
  public boolean isSelfExecutable(Job job) throws BindingException {
    Draft4JobApp app = (Draft4JobApp) loadAppObject(job.getApp());
    return app.getType().equals(Draft4JobAppType.EXPRESSION_TOOL);
  }

  @Override
  public void validate(Job job) throws BindingException {
    Draft4Job draft2Job = Draft4JobHelper.getDraft4Job(job);

    boolean throwException = false;
    StringBuilder builder = new StringBuilder("Missing inputs: ");

    Draft4JobApp draft2JobApp = draft2Job.getApp();
    for (Draft4InputPort inputPort : draft2JobApp.getInputs()) {
      if (Draft4SchemaHelper.isRequired(inputPort.getSchema())) {
        String inputPortId = Draft4SchemaHelper.normalizeId(inputPort.getId());
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
