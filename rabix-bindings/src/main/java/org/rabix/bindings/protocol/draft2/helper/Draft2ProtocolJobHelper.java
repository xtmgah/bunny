package org.rabix.bindings.protocol.draft2.helper;

import java.io.IOException;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolJobHelper;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.draft2.Draft2JobProcessor;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.bean.Draft2JobApp;
import org.rabix.bindings.protocol.draft2.bean.Draft2JobAppType;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;

public class Draft2ProtocolJobHelper implements ProtocolJobHelper {

  public Draft2Job getJob(Job job) throws BindingException {
    try {
      String appStr = job.getApp();
      String resolvedAppStr = URIHelper.getData(appStr);
      Draft2JobApp app = BeanSerializer.deserialize(JSONHelper.transformToJSON(resolvedAppStr), Draft2JobApp.class);
      return new Draft2JobProcessor().process(new Draft2Job(app, job.getInputs()));
    } catch (IOException e) {
      throw new BindingException(e);
    }
  }

  public boolean isSelfExecutable(Job job) throws BindingException {
    Draft2JobApp app = BeanSerializer.deserialize(JSONHelper.writeObject(job.getApp(Draft2JobApp.class)), Draft2JobApp.class);
    return app.getType().equals(Draft2JobAppType.EXPRESSION_TOOL);
  }

}
