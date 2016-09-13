package org.rabix.bindings.cwl1;

import java.util.Map;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFileValueProcessor;
import org.rabix.bindings.cwl1.bean.Draft3Job;
import org.rabix.bindings.cwl1.helper.Draft3JobHelper;
import org.rabix.bindings.cwl1.processor.Draft3PortProcessorException;
import org.rabix.bindings.cwl1.processor.callback.Draft3PortProcessorHelper;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;

public class Draft3FileValueProcessor implements ProtocolFileValueProcessor {

  public Set<FileValue> getInputFiles(Job job) throws BindingException {
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    try {
      return new Draft3PortProcessorHelper(draft3Job).getInputFiles(job.getInputs(), null, null);
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }
  
  @Override
  public Set<FileValue> getInputFiles(Job job, FileMapper fileMapper) throws BindingException {
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    try {
      return new Draft3PortProcessorHelper(draft3Job).getInputFiles(job.getInputs(), fileMapper, job.getConfig());
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
      return new Draft3PortProcessorHelper(draft3Job).getOutputFiles(job.getOutputs(), visiblePorts);
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateInputFiles(Job job, Set<FileValue> inputFiles) throws BindingException {
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    Map<String, Object> inputs;
    try {
      inputs = new Draft3PortProcessorHelper(draft3Job).updateInputFiles(job.getInputs(), inputFiles);
      return Job.cloneWithInputs(job, inputs);
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateOutputFiles(Job job, Set<FileValue> outputFiles) throws BindingException {
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    Map<String, Object> outputs;
    try {
      outputs = new Draft3PortProcessorHelper(draft3Job).updateOutputFiles(job.getOutputs(), outputFiles);
      return Job.cloneWithOutputs(job, outputs);
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getFlattenedInputFiles(Job job) throws BindingException {
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    try {
      return new Draft3PortProcessorHelper(draft3Job).flattenInputFiles(job.getInputs());
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getFlattenedOutputFiles(Job job, boolean onlyVisiblePorts) throws BindingException {
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


}