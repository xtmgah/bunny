package org.rabix.bindings.protocol.draft2.helper;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolJobHelper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.draft2.Draft2DocumentReferenceResolver;
import org.rabix.bindings.protocol.draft2.Draft2JobProcessor;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.bean.Draft2JobApp;
import org.rabix.bindings.protocol.draft2.bean.Draft2JobAppType;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;

public class Draft2ProtocolJobHelper implements ProtocolJobHelper {

  private final Draft2DocumentReferenceResolver documentResolver;

  public Draft2ProtocolJobHelper() {
    this.documentResolver = new Draft2DocumentReferenceResolver();
  }
  
  public Draft2Job getJob(Job job) throws BindingException {
    String resolvedAppStr = documentResolver.resolve(job.getApp());
    Draft2JobApp app = BeanSerializer.deserialize(JSONHelper.transformToJSON(resolvedAppStr), Draft2JobApp.class);
    return new Draft2JobProcessor().process(new Draft2Job(app, job.getInputs()));
  }

  public boolean isSelfExecutable(Job job) throws BindingException {
    Draft2JobApp app = BeanSerializer.deserialize(JSONHelper.writeObject(job.getApp(Draft2JobApp.class)), Draft2JobApp.class);
    return app.getType().equals(Draft2JobAppType.EXPRESSION_TOOL);
  }

}
