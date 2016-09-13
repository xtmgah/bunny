package org.rabix.bindings.cwl1.helper;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.cwl1.CWL1JobProcessor;
import org.rabix.bindings.cwl1.bean.CWL1Job;
import org.rabix.bindings.cwl1.bean.CWL1JobApp;
import org.rabix.bindings.cwl1.resolver.CWL1DocumentResolver;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;

public class CWL1JobHelper {

  public static CWL1Job getCWL1Job(Job job) throws BindingException {
    String resolvedAppStr = CWL1DocumentResolver.resolve(job.getApp());
    CWL1JobApp app = BeanSerializer.deserialize(JSONHelper.transformToJSON(resolvedAppStr), CWL1JobApp.class);
    return new CWL1JobProcessor().process(new CWL1Job(app, job.getInputs()));
  }
  
}
