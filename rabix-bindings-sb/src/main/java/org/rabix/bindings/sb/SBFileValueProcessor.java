package org.rabix.bindings.sb;

import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFileValueProcessor;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.helper.SBJobHelper;
import org.rabix.bindings.sb.processor.SBPortProcessorException;
import org.rabix.bindings.sb.processor.callback.SBPortProcessorHelper;

public class SBFileValueProcessor implements ProtocolFileValueProcessor {

  public Set<FileValue> getInputFiles(Job job) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    try {
      return new SBPortProcessorHelper(sbJob).flattenInputFiles(job.getInputs());
    } catch (SBPortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getOutputFiles(Job job) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    try {
      return new SBPortProcessorHelper(sbJob).flattenOutputFiles(job.getOutputs());
    } catch (SBPortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
