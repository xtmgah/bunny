package org.rabix.bindings;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
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
  public boolean canExecute(Job job) throws BindingException {
    return protocolJobHelper.isSelfExecutable(job);
  }
  
  @Override
  public boolean isSuccessful(Job job, int statusCode) throws BindingException {
    return resultCollector.isSuccessful(job, statusCode);
  }

  @Override
  public Job postprocess(Job job, File workingDir) throws BindingException {
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
  public List<String> buildCommandLineParts(Job job) throws BindingException {
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
  public void validate(Job job) throws BindingException {
    protocolJobHelper.validate(job);
  }
  
  @Override
  public ProtocolType getProtocolType() {
    return protocolType;
  }

}
