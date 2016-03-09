package org.rabix.bindings.protocol.draft2.helper;

import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.Executable;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.bean.Draft2JobApp;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;

public class Draft2ExecutableHelper {

  @SuppressWarnings("unchecked")
  public static Draft2Job convertToJob(Executable executable) throws BindingException {
    Draft2JobApp app = BeanSerializer.deserialize(JSONHelper.writeObject(executable.getApp(Draft2JobApp.class)), Draft2JobApp.class);
    return new Draft2Job(app, executable.getInputs(Map.class));
  }

}
