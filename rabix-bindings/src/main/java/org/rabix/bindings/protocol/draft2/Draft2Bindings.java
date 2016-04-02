package org.rabix.bindings.protocol.draft2;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.ProtocolCommandLineBuilder;
import org.rabix.bindings.ProtocolDocumentResolver;
import org.rabix.bindings.ProtocolAppProcessor;
import org.rabix.bindings.ProtocolPreprocessor;
import org.rabix.bindings.ProtocolTranslator;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.ProtocolValueProcessor;
import org.rabix.bindings.ProtocolRequirementProvider;
import org.rabix.bindings.ProtocolPostprocessor;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.protocol.draft2.resolver.Draft2DocumentResolver;

public class Draft2Bindings implements Bindings {

  private final ProtocolType protocolType;
  
  private final ProtocolTranslator translator;
  private final ProtocolAppProcessor appProcessor;
  private final ProtocolValueProcessor valueProcessor;
  
  private final ProtocolPreprocessor preprocessor;
  private final ProtocolPostprocessor postprocessor;
  
  private final ProtocolCommandLineBuilder commandLineBuilder;
  private final ProtocolRequirementProvider requirementProvider;
  
  private final ProtocolDocumentResolver documentResolver;
  
  public Draft2Bindings() throws BindingException {
    this.protocolType = ProtocolType.DRAFT2;
    this.postprocessor = new Draft2Postprocessor();
    this.preprocessor = new Draft2Preprocessor();
    this.commandLineBuilder = new Draft2CommandLineBuilder();
    this.valueProcessor = new Draft2ValueProcessor();
    this.translator = new Draft2Translator();
    this.requirementProvider = new Draft2RequirementProvider();
    this.appProcessor = new Draft2AppProcessor();
    this.documentResolver = new Draft2DocumentResolver();
  }
  
  @Override
  public String loadApp(String uri) throws BindingException {
    return documentResolver.resolve(uri);
  }
  
  @Override
  public Object loadAppObject(String uri) throws BindingException {
    return appProcessor.getAppObject(loadApp(uri));
  }
  
  @Override
  public boolean canExecute(Job job) throws BindingException {
    return appProcessor.isSelfExecutable(job);
  }
  
  @Override
  public boolean isSuccessful(Job job, int statusCode) throws BindingException {
    return postprocessor.isSuccessful(job, statusCode);
  }

  @Override
  public Job postprocess(Job job, File workingDir) throws BindingException {
    return postprocessor.postprocess(job, workingDir);
  }

  @Override
  public Job preprocess(Job job, File workingDir) throws BindingException {
    return preprocessor.preprocess(job, workingDir);
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
    return valueProcessor.getInputFiles(job);
  }

  @Override
  public Set<FileValue> getOutputFiles(Job job) throws BindingException {
    return valueProcessor.getOutputFiles(job);
  }
  
  @Override
  public Job mapInputFilePaths(Job job, FileMapper fileMapper) throws BindingException {
    return preprocessor.mapInputFilePaths(job, fileMapper);
  }

  @Override
  public Job mapOutputFilePaths(Job job, FileMapper fileMapper) throws BindingException {
    return preprocessor.mapOutputFilePaths(job, fileMapper);
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
    return translator.translateToDAG(job);
  }

  @Override
  public void validate(Job job) throws BindingException {
    appProcessor.validate(job);
  }
  
  @Override
  public ProtocolType getProtocolType() {
    return protocolType;
  }

}
