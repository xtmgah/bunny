package org.rabix.bindings.protocol.draft3;

import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFileValueProcessor;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.draft3.bean.Draft3Job;
import org.rabix.bindings.protocol.draft3.helper.Draft3JobHelper;
import org.rabix.bindings.protocol.draft3.processor.Draft3PortProcessorException;
import org.rabix.bindings.protocol.draft3.processor.callback.Draft3PortProcessorHelper;

public class Draft3FileValueProcessor implements ProtocolFileValueProcessor {

  public Set<FileValue> getInputFiles(Job job) throws BindingException {
    Draft3Job draft2Job = Draft3JobHelper.getDraft3Job(job);
    try {
      return new Draft3PortProcessorHelper(draft2Job).flattenInputFiles(job.getInputs());
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getOutputFiles(Job job) throws BindingException {
    Draft3Job draft2Job = Draft3JobHelper.getDraft3Job(job);
    try {
      return new Draft3PortProcessorHelper(draft2Job).flattenOutputFiles(job.getInputs());
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
