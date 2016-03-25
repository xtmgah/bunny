package org.rabix.bindings.protocol.draft2.helper;

import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolJobHelper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.bean.Draft2JobApp;
import org.rabix.bindings.protocol.draft2.bean.Draft2JobAppType;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;

public class Draft2ProtocolJobHelper implements ProtocolJobHelper {

  @Override
  public Object getApp(Job job) throws BindingException {
    return BeanSerializer.deserialize(JSONHelper.writeObject(job.getApp(Draft2JobApp.class)), Draft2JobApp.class);
  }
  
  @SuppressWarnings("unchecked")
  public Draft2Job getJob(Job job) throws BindingException {
    Draft2JobApp app = BeanSerializer.deserialize(JSONHelper.writeObject(job.getApp(Draft2JobApp.class)), Draft2JobApp.class);
    return new Draft2Job(app, job.getInputs(Map.class));
  }

  public boolean isSelfExecutable(Job job) {
    Draft2JobApp app = BeanSerializer.deserialize(JSONHelper.writeObject(job.getApp(Draft2JobApp.class)), Draft2JobApp.class);
    return app.getType().equals(Draft2JobAppType.EXPRESSION_TOOL);
  }


}
