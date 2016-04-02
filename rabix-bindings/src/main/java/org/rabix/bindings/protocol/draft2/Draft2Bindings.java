package org.rabix.bindings.protocol.draft2;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.CommandLineBuilder;
import org.rabix.bindings.DocumentReferenceResolver;
import org.rabix.bindings.ProtocolJobHelper;
import org.rabix.bindings.ProtocolProcessor;
import org.rabix.bindings.ProtocolTranslator;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.ProtocolValueOperator;
import org.rabix.bindings.RequirementProvider;
import org.rabix.bindings.ResultCollector;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.protocol.draft2.helper.Draft2ProtocolJobHelper;
import org.rabix.bindings.protocol.draft2.resolver.Draft2DocumentReferenceResolver;

public class Draft2Bindings implements Bindings {

  private final ProtocolType protocolType;
  
  private final ResultCollector resultCollector;
  private final ProtocolProcessor protocolProcessor;
  private final ProtocolValueOperator valueOperator;
  private final CommandLineBuilder commandLineBuilder;
  private final ProtocolTranslator protocolTranslator;
  private final RequirementProvider requirementProvider;
  private final ProtocolJobHelper protocolJobHelper;
  private final DocumentReferenceResolver documentReferenceResolver;
  
  public Draft2Bindings() throws BindingException {
    this.protocolType = ProtocolType.DRAFT2;
    this.resultCollector = new Draft2ResultCollector();
    this.protocolProcessor = new Draft2ProtocolProcessor();
    this.commandLineBuilder = new Draft2CommandLineBuilder();
    this.valueOperator = new Draft2ProtocolValueExtractor();
    this.protocolTranslator = new Draft2ProtocolTranslator();
    this.requirementProvider = new Draft2RequirementProvider();
    this.protocolJobHelper = new Draft2ProtocolJobHelper();
    this.documentReferenceResolver = new Draft2DocumentReferenceResolver();
  }
  
  @Override
  public String loadApp(String uri) throws BindingException {
    return documentReferenceResolver.resolve(uri);
  }
  
  @Override
  public Object loadAppObject(String uri) throws BindingException {
    return protocolJobHelper.getAppObject(loadApp(uri));
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
