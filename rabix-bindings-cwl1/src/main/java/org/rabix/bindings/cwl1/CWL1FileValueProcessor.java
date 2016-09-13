package org.rabix.bindings.cwl1;

import java.util.Map;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFileValueProcessor;
import org.rabix.bindings.cwl1.bean.CWL1Job;
import org.rabix.bindings.cwl1.helper.CWL1JobHelper;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorException;
import org.rabix.bindings.cwl1.processor.callback.CWL1PortProcessorHelper;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;

public class CWL1FileValueProcessor implements ProtocolFileValueProcessor {

  public Set<FileValue> getInputFiles(Job job) throws BindingException {
    CWL1Job cwl1Job = CWL1JobHelper.getCWL1Job(job);
    try {
      return new CWL1PortProcessorHelper(cwl1Job).getInputFiles(job.getInputs(), null, null);
    } catch (CWL1PortProcessorException e) {
      throw new BindingException(e);
    }
  }
  
  @Override
  public Set<FileValue> getInputFiles(Job job, FileMapper fileMapper) throws BindingException {
    CWL1Job cwl1Job = CWL1JobHelper.getCWL1Job(job);
    try {
      return new CWL1PortProcessorHelper(cwl1Job).getInputFiles(job.getInputs(), fileMapper, job.getConfig());
    } catch (CWL1PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getOutputFiles(Job job, boolean onlyVisiblePorts) throws BindingException {
    CWL1Job cwl1Job = CWL1JobHelper.getCWL1Job(job);
    try {
      Set<String> visiblePorts = null;
      if (onlyVisiblePorts) {
        visiblePorts = job.getVisiblePorts();
      }
      return new CWL1PortProcessorHelper(cwl1Job).getOutputFiles(job.getOutputs(), visiblePorts);
    } catch (CWL1PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateInputFiles(Job job, Set<FileValue> inputFiles) throws BindingException {
    CWL1Job cwl1Job = CWL1JobHelper.getCWL1Job(job);
    Map<String, Object> inputs;
    try {
      inputs = new CWL1PortProcessorHelper(cwl1Job).updateInputFiles(job.getInputs(), inputFiles);
      return Job.cloneWithInputs(job, inputs);
    } catch (CWL1PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job updateOutputFiles(Job job, Set<FileValue> outputFiles) throws BindingException {
    CWL1Job cwl1Job = CWL1JobHelper.getCWL1Job(job);
    Map<String, Object> outputs;
    try {
      outputs = new CWL1PortProcessorHelper(cwl1Job).updateOutputFiles(job.getOutputs(), outputFiles);
      return Job.cloneWithOutputs(job, outputs);
    } catch (CWL1PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getFlattenedInputFiles(Job job) throws BindingException {
    CWL1Job cwl1Job = CWL1JobHelper.getCWL1Job(job);
    try {
      return new CWL1PortProcessorHelper(cwl1Job).flattenInputFiles(job.getInputs());
    } catch (CWL1PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Set<FileValue> getFlattenedOutputFiles(Job job, boolean onlyVisiblePorts) throws BindingException {
    CWL1Job cwl1Job = CWL1JobHelper.getCWL1Job(job);
    try {
      Set<String> visiblePorts = null;
      if (onlyVisiblePorts) {
        visiblePorts = job.getVisiblePorts();
      }
      return new CWL1PortProcessorHelper(cwl1Job).flattenOutputFiles(job.getOutputs(), visiblePorts);
    } catch (CWL1PortProcessorException e) {
      throw new BindingException(e);
    }
  }


}
