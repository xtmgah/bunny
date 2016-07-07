package org.rabix.bindings.protocol.draft4;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.ProtocolAppProcessor;
import org.rabix.bindings.ProtocolCommandLineBuilder;
import org.rabix.bindings.ProtocolFilePathMapper;
import org.rabix.bindings.ProtocolFileValueProcessor;
import org.rabix.bindings.ProtocolProcessor;
import org.rabix.bindings.ProtocolRequirementProvider;
import org.rabix.bindings.ProtocolTranslator;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.model.requirement.ResourceRequirement;
import org.rabix.bindings.protocol.draft4.bean.Draft4JobApp;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;

public class Draft4Bindings implements Bindings {

  private final ProtocolType protocolType;
  
  private final ProtocolTranslator translator;
  private final ProtocolAppProcessor appProcessor;
  private final ProtocolFileValueProcessor fileValueProcessor;
  
  private final ProtocolProcessor processor;
  private final ProtocolFilePathMapper filePathMapper;
  
  private final ProtocolCommandLineBuilder commandLineBuilder;
  private final ProtocolRequirementProvider requirementProvider;
  
  public Draft4Bindings() throws BindingException {
    this.protocolType = ProtocolType.DRAFT4;
    this.filePathMapper = new Draft4FilePathMapper();
    this.processor = new Draft4Processor();
    this.commandLineBuilder = new Draft4CommandLineBuilder();
    this.fileValueProcessor = new Draft4FileValueProcessor();
    this.translator = new Draft4Translator();
    this.requirementProvider = new Draft4RequirementProvider();
    this.appProcessor = new Draft4AppProcessor();
  }
  
  @Override
  public String loadApp(String uri) throws BindingException {
    return appProcessor.loadApp(uri);
  }
  
  @Override
  public Application loadAppObject(String uri) throws BindingException {
    Draft4JobApp application = (Draft4JobApp) appProcessor.loadAppObject(uri);
    if (!Draft4JobApp.DRAFT_4_VERSION.equals(application.getCwlVersion())) {
      throw new BindingException(uri + " is not an CWL Draft-3 application");
    }
    return application;
  }
  
  @Override
  public boolean canExecute(Job job) throws BindingException {
    return appProcessor.isSelfExecutable(job);
  }
  
  @Override
  public Job preprocess(Job job, File workingDir) throws BindingException {
    return processor.preprocess(job, workingDir);
  }
  
  @Override
  public boolean isSuccessful(Job job, int statusCode) throws BindingException {
    return processor.isSuccessful(job, statusCode);
  }

  @Override
  public Job postprocess(Job job, File workingDir) throws BindingException {
    return processor.postprocess(job, workingDir);
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
    return fileValueProcessor.getInputFiles(job);
  }

  @Override
  public Set<FileValue> getOutputFiles(Job job) throws BindingException {
    return fileValueProcessor.getOutputFiles(job);
  }
  
  @Override
  public Job mapInputFilePaths(Job job, FileMapper fileMapper) throws BindingException {
    return filePathMapper.mapInputFilePaths(job, fileMapper);
  }

  @Override
  public Job mapOutputFilePaths(Job job, FileMapper fileMapper) throws BindingException {
    return filePathMapper.mapOutputFilePaths(job, fileMapper);
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
  public ResourceRequirement getResourceRequirement(Job job) throws BindingException {
    return requirementProvider.getResourceRequirement(job);
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

  @Override
  public Job postprocess(Job job, File workingDir, HashAlgorithm hashAlgorithm, boolean setFilename, boolean setSize,
      HashAlgorithm secondaryFilesHashAlgorithm, boolean secondaryFilesSetFilename, boolean secondaryFilesSetSize)
          throws BindingException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object transformInputs(Object value, Job job, Object transform) throws BindingException {
    // TODO Auto-generated method stub
    return null;
  }

}
