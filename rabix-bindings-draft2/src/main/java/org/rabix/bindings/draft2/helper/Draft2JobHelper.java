package org.rabix.bindings.draft2.helper;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.draft2.Draft2JobProcessor;
import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.bean.Draft2JobApp;
import org.rabix.bindings.draft2.bean.Draft2Resources;
import org.rabix.bindings.draft2.resolver.Draft2DocumentResolver;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;

public class Draft2JobHelper {

  public static Draft2Job getDraft2Job(Job job) throws BindingException {
    String resolvedAppStr = Draft2DocumentResolver.resolve(job.getApp());
    Draft2JobApp app = BeanSerializer.deserialize(JSONHelper.transformToJSON(resolvedAppStr), Draft2JobApp.class);
    Draft2Job draft2Job = new Draft2JobProcessor().process(new Draft2Job(app, job.getInputs()));

    if (job.getResources() != null) {
      Draft2Resources draft2Resources = new Draft2Resources(false, job.getResources().getCpu(), job.getResources().getMemMB());
      draft2Job.setResources(draft2Resources);
    }
    return draft2Job;
  }
  
}
