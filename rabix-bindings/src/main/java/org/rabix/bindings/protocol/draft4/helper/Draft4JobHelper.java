package org.rabix.bindings.protocol.draft4.helper;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.draft4.Draft4JobProcessor;
import org.rabix.bindings.protocol.draft4.bean.Draft4Job;
import org.rabix.bindings.protocol.draft4.bean.Draft4JobApp;
import org.rabix.bindings.protocol.draft4.resolver.Draft4DocumentResolver;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;

public class Draft4JobHelper {

  public static Draft4Job getDraft4Job(Job job) throws BindingException {
    String resolvedAppStr = Draft4DocumentResolver.resolve(job.getApp());
    Draft4JobApp app = BeanSerializer.deserialize(JSONHelper.transformToJSON(resolvedAppStr), Draft4JobApp.class);
    return new Draft4JobProcessor().process(new Draft4Job(app, job.getInputs()));
  }
  
}
