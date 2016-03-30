package org.rabix.bindings.protocol.draft2;

import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolValueOperator;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.helper.Draft2ProtocolJobHelper;
import org.rabix.bindings.protocol.draft2.processor.Draft2PortProcessorException;
import org.rabix.bindings.protocol.draft2.processor.callback.Draft2PortProcessorHelper;

public class Draft2ProtocolValueExtractor implements ProtocolValueOperator {

  public Set<FileValue> getInputFiles(Job job) throws BindingException {
    Draft2Job draft2Job = new Draft2ProtocolJobHelper().getDraft2Job(job);
    try {
      return new Draft2PortProcessorHelper(draft2Job).flattenInputFiles(job.getInputs());
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getOutputFiles(Job job) throws BindingException {
    Draft2Job draft2Job = new Draft2ProtocolJobHelper().getDraft2Job(job);
    try {
      return new Draft2PortProcessorHelper(draft2Job).flattenOutputFiles(job.getInputs());
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
