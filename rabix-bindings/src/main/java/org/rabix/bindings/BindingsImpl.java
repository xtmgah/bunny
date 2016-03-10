package org.rabix.bindings;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.protocol.draft2.helper.Draft2ExecutableHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingsImpl implements Bindings {

  private static final Logger logger = LoggerFactory.getLogger(BindingsImpl.class);
  
  private final ProtocolType protocolType;
  
  private final ResultCollector resultCollector;
  private final ProtocolProcessor protocolProcessor;
  private final ProtocolValueOperator valueOperator;
  private final CommandLineBuilder commandLineBuilder;
  private final ProtocolTranslator protocolTranslator;
  private final RequirementProvider requirementProvider;
  private final DocumentReferenceResolver documentReferenceResolver;
  
  public BindingsImpl(ProtocolType protocolType) throws BindingException {
    try {
      this.protocolType = protocolType;
      this.resultCollector = ResultCollectorFactory.create(protocolType);
      this.protocolProcessor = ProtocolProcessorFactory.create(protocolType);
      this.commandLineBuilder = CommandLineBuilderFactory.create(protocolType);
      this.valueOperator = ProtocolValueOperatorFactory.create(protocolType);
      this.protocolTranslator = ProtocolTranslatorFactory.create(protocolType);
      this.requirementProvider = RequirementProviderFactory.create(protocolType);
      this.documentReferenceResolver = DocumentReferenceResolverFactory.create(protocolType);
    } catch (BindingException e) {
      logger.error("Failed to create Bindings for type " + protocolType, e);
      throw new BindingException("Failed to create Bindings for type " + protocolType);
    }
  }
  
  @Override
  public String loadAppFromFile(File file) throws BindingException {
    return documentReferenceResolver.resolve(file);
  }
  
  @Override
  public Object getApp(Executable executable) throws BindingException {
    return Draft2ExecutableHelper.convertToJob(executable).getApp();
  }
  
  @Override
  public boolean isSuccessfull(Executable executable, int statusCode) throws BindingException {
    return resultCollector.isSuccessfull(executable, statusCode);
  }

  @Override
  public Executable populateOutputs(Executable executable, File workingDir) throws BindingException {
    return resultCollector.populateOutputs(executable, workingDir);
  }

  @Override
  public Executable preprocess(Executable executable, File workingDir) throws BindingException {
    return protocolProcessor.preprocess(executable, workingDir);
  }

  @Override
  public String buildCommandLine(Executable executable) throws BindingException {
    return commandLineBuilder.buildCommandLine(executable);
  }

  @Override
  public List<Object> buildCommandLineParts(Executable executable) throws BindingException {
    return commandLineBuilder.buildCommandLineParts(executable);
  }

  @Override
  public Set<FileValue> getInputFiles(Executable executable) throws BindingException {
    return valueOperator.getInputFiles(executable);
  }

  @Override
  public Set<FileValue> getOutputFiles(Executable executable) throws BindingException {
    return valueOperator.getOutputFiles(executable);
  }
  
  @Override
  public Object addToInputs(Object inputs, String id, Object value) throws BindingException {
    return valueOperator.addToInputs(inputs, id, value);
  }
  
  @Override
  public Object addToOutputs(Object outputs, String id, Object value) throws BindingException {
    return valueOperator.addToOutputs(outputs, id, value);
  }

  @Override
  public Executable mapInputFilePaths(Executable executable, FileMapper fileMapper) throws BindingException {
    return protocolProcessor.mapInputFilePaths(executable, fileMapper);
  }

  @Override
  public Executable mapOutputFilePaths(Executable executable, FileMapper fileMapper) throws BindingException {
    return protocolProcessor.mapOutputFilePaths(executable, fileMapper);
  }

  @Override
  public DockerContainerRequirement getDockerRequirement(Executable executable) throws BindingException {
    return requirementProvider.getDockerRequirement(executable);
  }

  @Override
  public EnvironmentVariableRequirement getEnvironmentVariableRequirement(Executable executable) throws BindingException {
    return requirementProvider.getEnvironmentVariableRequirement(executable);
  }

  @Override
  public FileRequirement getFileRequirement(Executable executable) throws BindingException {
    return requirementProvider.getFileRequirement(executable);
  }
  
  @Override
  public Executable populateResources(Executable executable) throws BindingException {
    return requirementProvider.populateResources(executable);
  }
  
  @Override
  public List<Requirement> getRequirements(Executable executable) throws BindingException {
    return requirementProvider.getRequirements(executable);
  }

  @Override
  public List<Requirement> getHints(Executable executable) throws BindingException {
    return requirementProvider.getHints(executable);
  }

  @Override
  public DAGNode translateToDAGFromPayload(String job) throws BindingException {
    return protocolTranslator.translateToDAGFromPayload(job);
  }
  
  @Override
  public Object translateInputs(String inputs) throws BindingException {
    return protocolTranslator.translateInputs(inputs);
  }
  
  @Override
  public DAGNode translateToDAG(String app, String inputs) throws BindingException {
    return protocolTranslator.translateToDAG(app, inputs);
  }

  @Override
  public Object translateInputsFromPayload(String job) {
    return protocolTranslator.translateInputsFromPayload(job);
  }

  @Override
  public Object getInputValueById(Object inputs, String id) {
    return valueOperator.getInputValueById(inputs, id);
  }

  @Override
  public Object getOutputValueById(Object outputs, String id) {
    return valueOperator.getOutputValueById(outputs, id);
  }

  @Override
  public ProtocolType getProtocolType() {
    return protocolType;
  }

}
