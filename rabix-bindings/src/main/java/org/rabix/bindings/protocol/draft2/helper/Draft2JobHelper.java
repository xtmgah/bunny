package org.rabix.bindings.protocol.draft2.helper;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.draft2.Draft2JobProcessor;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.bean.Draft2JobApp;
import org.rabix.bindings.protocol.draft2.resolver.Draft2DocumentResolver;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;

public class Draft2JobHelper {

  public static Draft2Job getDraft2Job(Job job) throws BindingException {
    String resolvedAppStr = Draft2DocumentResolver.resolve(job.getApp());
    Draft2JobApp app = BeanSerializer.deserialize(JSONHelper.transformToJSON(resolvedAppStr), Draft2JobApp.class);
    return new Draft2JobProcessor().process(new Draft2Job(app, job.getInputs()));
  }
  
}
