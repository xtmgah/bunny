package org.rabix.bindings.draft3;

import java.util.Map;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFileValueProcessor;
import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.helper.Draft3JobHelper;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorException;
import org.rabix.bindings.draft3.processor.callback.Draft3PortProcessorHelper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;

public class Draft3FileValueProcessor implements ProtocolFileValueProcessor {

  public Set<FileValue> getInputFiles(Job job) throws BindingException {
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    try {
      return new Draft3PortProcessorHelper(draft3Job).flattenInputFiles(job.getInputs());
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getOutputFiles(Job job, boolean onlyVisiblePorts) throws BindingException {
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    try {
      Set<String> visiblePorts = null;
      if (onlyVisiblePorts) {
        visiblePorts = job.getVisiblePorts();
      }
      return new Draft3PortProcessorHelper(draft3Job).flattenOutputFiles(job.getOutputs(), visiblePorts);
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateInputFiles(Job job, Set<FileValue> inputFiles) throws BindingException {
    Draft3Job draft2Job = Draft3JobHelper.getDraft3Job(job);
    Map<String, Object> inputs;
    try {
      inputs = new Draft3PortProcessorHelper(draft2Job).updateInputFiles(job.getInputs(), inputFiles);
      return Job.cloneWithInputs(job, inputs);
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateOutputFiles(Job job, Set<FileValue> outputFiles) throws BindingException {
    Draft3Job draft2Job = Draft3JobHelper.getDraft3Job(job);
    Map<String, Object> outputs;
    try {
      outputs = new Draft3PortProcessorHelper(draft2Job).updateOutputFiles(job.getOutputs(), outputFiles);
      return Job.cloneWithOutputs(job, outputs);
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
