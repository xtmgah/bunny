package org.rabix.bindings.cwl1;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolAppProcessor;
import org.rabix.bindings.cwl1.bean.CWL1InputPort;
import org.rabix.bindings.cwl1.bean.CWL1Job;
import org.rabix.bindings.cwl1.bean.CWL1JobApp;
import org.rabix.bindings.cwl1.bean.CWL1JobAppType;
import org.rabix.bindings.cwl1.helper.CWL1JobHelper;
import org.rabix.bindings.cwl1.helper.CWL1SchemaHelper;
import org.rabix.bindings.cwl1.resolver.CWL1DocumentResolver;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.Job;
import org.rabix.common.json.BeanSerializer;

public class CWL1AppProcessor implements ProtocolAppProcessor {

  @Override
  public String loadApp(String uri) throws BindingException {
    return CWL1DocumentResolver.resolve(uri);
  }
  
  @Override
  public Application loadAppObject(String app) throws BindingException {
    return BeanSerializer.deserialize(loadApp(app), CWL1JobApp.class);
  }

  @Override
  public boolean isSelfExecutable(Job job) throws BindingException {
    CWL1JobApp app = (CWL1JobApp) loadAppObject(job.getApp());
    return app.getType().equals(CWL1JobAppType.EXPRESSION_TOOL);
  }

  @Override
  public void validate(Job job) throws BindingException {
    CWL1Job draft2Job = CWL1JobHelper.getCWL1Job(job);

    boolean throwException = false;
    StringBuilder builder = new StringBuilder("Missing inputs: ");

    CWL1JobApp draft2JobApp = draft2Job.getApp();
    for (CWL1InputPort inputPort : draft2JobApp.getInputs()) {
      if (CWL1SchemaHelper.isRequired(inputPort.getSchema())) {
        String inputPortId = CWL1SchemaHelper.normalizeId(inputPort.getId());
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
