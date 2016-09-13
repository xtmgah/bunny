package org.rabix.bindings.cwl1;

import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFilePathMapper;
import org.rabix.bindings.cwl1.bean.CWL1Job;
import org.rabix.bindings.cwl1.helper.CWL1JobHelper;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessor;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorException;
import org.rabix.bindings.cwl1.processor.callback.CWL1FilePathMapProcessorCallback;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.Job;

public class CWL1FilePathMapper implements ProtocolFilePathMapper {

  @Override
  public Job mapInputFilePaths(final Job job, final FileMapper fileMapper) throws BindingException {
    CWL1Job draft2Job = CWL1JobHelper.getCWL1Job(job);
    
    CWL1PortProcessor draft2PortProcessor = new CWL1PortProcessor(draft2Job);
    try {
      Map<String, Object> config = job.getConfig();
      Map<String, Object> inputs = draft2PortProcessor.processInputs(job.getInputs(), new CWL1FilePathMapProcessorCallback(fileMapper, config));
      return Job.cloneWithInputs(job, inputs);
    } catch (CWL1PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job mapOutputFilePaths(final Job job, final FileMapper fileMapper) throws BindingException {
    CWL1Job draft2Job = CWL1JobHelper.getCWL1Job(job);
    
    CWL1PortProcessor draft2PortProcessor = new CWL1PortProcessor(draft2Job);
    try {
      Map<String, Object> config = job.getConfig();
      Map<String, Object> outputs = draft2PortProcessor.processOutputs(job.getOutputs(), new CWL1FilePathMapProcessorCallback(fileMapper, config));
      return Job.cloneWithOutputs(job, outputs);
    } catch (CWL1PortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
