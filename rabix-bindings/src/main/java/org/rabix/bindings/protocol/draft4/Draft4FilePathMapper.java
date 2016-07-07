package org.rabix.bindings.protocol.draft4;

import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolFilePathMapper;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.draft4.bean.Draft4Job;
import org.rabix.bindings.protocol.draft4.helper.Draft4JobHelper;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessor;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessorException;
import org.rabix.bindings.protocol.draft4.processor.callback.Draft4FilePathMapProcessorCallback;

public class Draft4FilePathMapper implements ProtocolFilePathMapper {

  @Override
  public Job mapInputFilePaths(final Job job, final FileMapper fileMapper) throws BindingException {
    Draft4Job draft2Job = Draft4JobHelper.getDraft4Job(job);
    
    Draft4PortProcessor draft2PortProcessor = new Draft4PortProcessor(draft2Job);
    try {
      Map<String, Object> inputs = draft2PortProcessor.processInputs(job.getInputs(), new Draft4FilePathMapProcessorCallback(fileMapper));
      return Job.cloneWithInputs(job, inputs);
    } catch (Draft4PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job mapOutputFilePaths(final Job job, final FileMapper fileMapper) throws BindingException {
    Draft4Job draft2Job = Draft4JobHelper.getDraft4Job(job);
    
    Draft4PortProcessor draft2PortProcessor = new Draft4PortProcessor(draft2Job);
    try {
      Map<String, Object> outputs = draft2PortProcessor.processOutputs(job.getOutputs(), new Draft4FilePathMapProcessorCallback(fileMapper));
      return Job.cloneWithOutputs(job, outputs);
    } catch (Draft4PortProcessorException e) {
      throw new BindingException(e);
    }
  }

}
