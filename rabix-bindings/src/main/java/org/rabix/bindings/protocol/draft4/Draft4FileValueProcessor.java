package org.rabix.bindings.protocol.draft4;

import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFileValueProcessor;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.draft4.bean.Draft4Job;
import org.rabix.bindings.protocol.draft4.helper.Draft4JobHelper;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessorException;
import org.rabix.bindings.protocol.draft4.processor.callback.Draft4PortProcessorHelper;

public class Draft4FileValueProcessor implements ProtocolFileValueProcessor {

  public Set<FileValue> getInputFiles(Job job) throws BindingException {
    Draft4Job draft2Job = Draft4JobHelper.getDraft4Job(job);
    try {
      return new Draft4PortProcessorHelper(draft2Job).flattenInputFiles(job.getInputs());
    } catch (Draft4PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getOutputFiles(Job job) throws BindingException {
    Draft4Job draft2Job = Draft4JobHelper.getDraft4Job(job);
    try {
      return new Draft4PortProcessorHelper(draft2Job).flattenOutputFiles(job.getInputs());
    } catch (Draft4PortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
