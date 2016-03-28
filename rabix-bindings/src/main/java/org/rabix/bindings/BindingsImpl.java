package org.rabix.bindings;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.Requirement;
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
  private final ProtocolJobHelper protocolJobHelper;
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
      this.protocolJobHelper = ProtocolJobHelperFactory.create(protocolType);
      this.documentReferenceResolver = DocumentReferenceResolverFactory.create(protocolType);
    } catch (BindingException e) {
      logger.error("Failed to create Bindings for type " + protocolType, e);
      throw new BindingException("Failed to create Bindings for type " + protocolType);
    }
  }
  
  @Override
  public String loadApp(String uri) throws BindingException {
    return documentReferenceResolver.resolve(uri);
  }
  
  @Override
  public Object getApp(Job job) throws BindingException {
    return protocolJobHelper.getApp(job);
  }
  
  @Override
  public boolean isSelfExecutable(Job job) throws BindingException {
    return protocolJobHelper.isSelfExecutable(job);
  }
  
  @Override
  public boolean isSuccessfull(Job job, int statusCode) throws BindingException {
    return resultCollector.isSuccessfull(job, statusCode);
  }

  @Override
  public Job populateOutputs(Job job, File workingDir) throws BindingException {
    return resultCollector.populateOutputs(job, workingDir);
  }

  @Override
  public Job preprocess(Job job, File workingDir) throws BindingException {
    return protocolProcessor.preprocess(job, workingDir);
  }

  @Override
  public String buildCommandLine(Job job) throws BindingException {
    return commandLineBuilder.buildCommandLine(job);
  }

  @Override
  public List<Object> buildCommandLineParts(Job job) throws BindingException {
    return commandLineBuilder.buildCommandLineParts(job);
  }

  @Override
  public Set<FileValue> getInputFiles(Job job) throws BindingException {
    return valueOperator.getInputFiles(job);
  }

  @Override
  public Set<FileValue> getOutputFiles(Job job) throws BindingException {
    return valueOperator.getOutputFiles(job);
  }
  
  @Override
  public Job mapInputFilePaths(Job job, FileMapper fileMapper) throws BindingException {
    return protocolProcessor.mapInputFilePaths(job, fileMapper);
  }

  @Override
  public Job mapOutputFilePaths(Job job, FileMapper fileMapper) throws BindingException {
    return protocolProcessor.mapOutputFilePaths(job, fileMapper);
  }

  @Override
  public DockerContainerRequirement getDockerRequirement(Job job) throws BindingException {
    return requirementProvider.getDockerRequirement(job);
  }

  @Override
  public EnvironmentVariableRequirement getEnvironmentVariableRequirement(Job job) throws BindingException {
    return requirementProvider.getEnvironmentVariableRequirement(job);
  }

  @Override
  public FileRequirement getFileRequirement(Job job) throws BindingException {
    return requirementProvider.getFileRequirement(job);
  }
  
  @Override
  public Job populateResources(Job job) throws BindingException {
    return requirementProvider.populateResources(job);
  }
  
  @Override
  public List<Requirement> getRequirements(Job job) throws BindingException {
    return requirementProvider.getRequirements(job);
  }

  @Override
  public List<Requirement> getHints(Job job) throws BindingException {
    return requirementProvider.getHints(job);
  }
  
  @Override
  public DAGNode translateToDAG(Job job) throws BindingException {
    return protocolTranslator.translateToDAG(job);
  }

  @Override
  public DAGNode translateToDAGFromPayload(String job) throws BindingException {
    return protocolTranslator.translateToDAGFromPayload(job);
  }
  
  @Override
  public Map<String, Object> translateInputs(String inputs) throws BindingException {
    return protocolTranslator.translateInputs(inputs);
  }
  
  @Override
  public DAGNode translateToDAG(String app, String inputs) throws BindingException {
    return protocolTranslator.translateToDAG(app, inputs);
  }

  @Override
  public Map<String, Object> translateInputsFromPayload(String job) {
    return protocolTranslator.translateInputsFromPayload(job);
  }

  @Override
  public ProtocolType getProtocolType() {
    return protocolType;
  }

}
