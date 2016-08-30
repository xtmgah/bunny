package org.rabix.bindings.draft2;

import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFileValueProcessor;
import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.helper.Draft2JobHelper;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorException;
import org.rabix.bindings.draft2.processor.callback.Draft2PortProcessorHelper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;

public class Draft2FileValueProcessor implements ProtocolFileValueProcessor {

  public Set<FileValue> getInputFiles(Job job) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);
    try {
      return new Draft2PortProcessorHelper(draft2Job).flattenInputFiles(job.getInputs());
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getOutputFiles(Job job) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);
    try {
      return new Draft2PortProcessorHelper(draft2Job).flattenOutputFiles(job.getInputs());
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
