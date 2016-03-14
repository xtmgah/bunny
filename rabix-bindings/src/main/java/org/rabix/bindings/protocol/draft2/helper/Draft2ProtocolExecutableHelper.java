package org.rabix.bindings.protocol.draft2.helper;

import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolExecutableHelper;
import org.rabix.bindings.model.Executable;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.bean.Draft2JobApp;
import org.rabix.bindings.protocol.draft2.bean.Draft2JobAppType;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;

public class Draft2ProtocolExecutableHelper implements ProtocolExecutableHelper {

  @Override
  public Object getApp(Executable executable) throws BindingException {
    return BeanSerializer.deserialize(JSONHelper.writeObject(executable.getApp(Draft2JobApp.class)), Draft2JobApp.class);
  }
  
  @SuppressWarnings("unchecked")
  public Draft2Job getJob(Executable executable) throws BindingException {
    Draft2JobApp app = BeanSerializer.deserialize(JSONHelper.writeObject(executable.getApp(Draft2JobApp.class)), Draft2JobApp.class);
    return new Draft2Job(app, executable.getInputs(Map.class));
  }

  public boolean isSelfExecutable(Executable executable) {
    Draft2JobApp app = BeanSerializer.deserialize(JSONHelper.writeObject(executable.getApp(Draft2JobApp.class)), Draft2JobApp.class);
    return app.getType().equals(Draft2JobAppType.EXPRESSION_TOOL);
  }


}
