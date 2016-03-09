package org.rabix.bindings.protocol.draft2;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolProcessor;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.Executable;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.helper.Draft2ExecutableHelper;
import org.rabix.bindings.protocol.draft2.processor.Draft2PortProcessor;
import org.rabix.bindings.protocol.draft2.processor.Draft2PortProcessorException;
import org.rabix.bindings.protocol.draft2.processor.callback.Draft2PortProcessorHelper;
import org.rabix.bindings.protocol.draft2.processor.callback.FilePathMapProcessorCallback;
import org.rabix.common.json.BeanSerializer;

public class Draft2ProtocolProcessor implements ProtocolProcessor {

  private static final String JOB_FILE = "job.json";
  
  @Override
  @SuppressWarnings("unchecked")
  public Executable preprocess(final Executable executable, final File workingDir) throws BindingException {
    Draft2Job draft2Job = Draft2ExecutableHelper.convertToJob(executable);
    Draft2PortProcessorHelper portProcessorHelper = new Draft2PortProcessorHelper(draft2Job);
    try {
      File jobFile = new File(workingDir, JOB_FILE);
      String serializedJob = BeanSerializer.serializePartial(Draft2ExecutableHelper.convertToJob(executable));
      FileUtils.writeStringToFile(jobFile, serializedJob);
      
      Map<String, Object> inputs = executable.getInputs(Map.class);
      inputs = portProcessorHelper.setFileSize(inputs);
      inputs = portProcessorHelper.loadInputContents(inputs);
      inputs = portProcessorHelper.stageInputFiles(inputs, workingDir);
      return Executable.cloneWithInputs(executable, inputs);
    } catch (Draft2PortProcessorException | IOException e) {
      throw new BindingException(e);
    }
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public Executable mapInputFilePaths(final Executable executable, final FileMapper fileMapper) throws BindingException {
    Draft2Job draft2Job = Draft2ExecutableHelper.convertToJob(executable);
    
    Draft2PortProcessor draft2PortProcessor = new Draft2PortProcessor(draft2Job);
    try {
      Map<String, Object> inputs = draft2PortProcessor.processInputs(executable.getInputs(Map.class), new FilePathMapProcessorCallback(fileMapper));
      return Executable.cloneWithInputs(executable, inputs);
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Executable mapOutputFilePaths(final Executable executable, final FileMapper fileMapper) throws BindingException {
    Draft2Job draft2Job = Draft2ExecutableHelper.convertToJob(executable);
    
    Draft2PortProcessor draft2PortProcessor = new Draft2PortProcessor(draft2Job);
    try {
      Map<String, Object> outputs = draft2PortProcessor.processOutputs(executable.getOutputs(Map.class), new FilePathMapProcessorCallback(fileMapper));
      return Executable.cloneWithOutputs(executable, outputs);
    } catch (Draft2PortProcessorException e) {
      throw new BindingException(e);
    }
  }
  
}
