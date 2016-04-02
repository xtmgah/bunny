package org.rabix.bindings.protocol.draft2;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolPreprocessor;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.processor.Draft2PortProcessor;
import org.rabix.bindings.protocol.draft2.processor.Draft2PortProcessorException;
import org.rabix.bindings.protocol.draft2.processor.callback.Draft2PortProcessorHelper;
import org.rabix.bindings.protocol.draft2.processor.callback.FilePathMapProcessorCallback;
import org.rabix.common.json.BeanSerializer;

public class Draft2Preprocessor implements ProtocolPreprocessor {

  private static final String JOB_FILE = "job.json";
  
  @Override
  public Job preprocess(final Job job, final File workingDir) throws BindingException {
    Draft2AppProcessor jobHelper = new Draft2AppProcessor();
    
    Draft2Job draft2Job = jobHelper.getDraft2Job(job);
    Draft2PortProcessorHelper portProcessorHelper = new Draft2PortProcessorHelper(draft2Job);
    try {
      File jobFile = new File(workingDir, JOB_FILE);
      String serializedJob = BeanSerializer.serializePartial(jobHelper.getDraft2Job(job));
      FileUtils.writeStringToFile(jobFile, serializedJob);
      
      Map<String, Object> inputs = job.getInputs();
      inputs = portProcessorHelper.setFileSize(inputs);
      inputs = portProcessorHelper.loadInputContents(inputs);
      inputs = portProcessorHelper.stageInputFiles(inputs, workingDir);
      return Job.cloneWithInputs(job, inputs);
    } catch (Draft2PortProcessorException | IOException e) {
      throw new BindingException(e);
    }
  }
  
  @Override
  public Job mapInputFilePaths(final Job job, final FileMapper fileMapper) throws BindingException {
    Draft2Job draft2Job = new Draft2AppProcessor().getDraft2Job(job);
    
    Draft2PortProcessor draft2PortProcessor = new Draft2PortProcessor(draft2Job);
    try {
      Map<String, Object> inputs = draft2PortProcessor.processInputs(job.getInputs(), new FilePathMapProcessorCallback(fileMapper));
      return Job.cloneWithInputs(job, inputs);
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job mapOutputFilePaths(final Job job, final FileMapper fileMapper) throws BindingException {
    Draft2Job draft2Job = new Draft2AppProcessor().getDraft2Job(job);
    
    Draft2PortProcessor draft2PortProcessor = new Draft2PortProcessor(draft2Job);
    try {
      Map<String, Object> outputs = draft2PortProcessor.processOutputs(job.getOutputs(), new FilePathMapProcessorCallback(fileMapper));
      return Job.cloneWithOutputs(job, outputs);
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }
  
}
