package org.rabix.bindings.protocol.rabix;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.filemapper.FileMappingException;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.protocol.rabix.helper.RabixHelper;
import org.rabix.common.helper.CloneHelper;

public class RabixBindings implements Bindings {

  private ProtocolType protocolType;
  private RabixCommandLineBuilder rabixCommandLineBuilder = new RabixCommandLineBuilder();;
  private final RabixTranslator rabixTranslator = new RabixTranslator();;

  public RabixBindings() throws BindingException {
    this.protocolType = ProtocolType.RABIX;
  }

  @Override
  public String loadApp(String appURI) throws BindingException {
    String app = null;
    try {
      app = URIHelper.getData(appURI);
    } catch (IOException e) {
      throw new BindingException(e);
    }
    return app;
  }

  @Override
  public Object loadAppObject(String appURI) throws BindingException {
    String app = loadApp(appURI);
    return RabixAppProcessor.loadAppObject(appURI, app);
  }

  @Override
  public boolean canExecute(Job job) throws BindingException {
    return false;
  }

  @Override
  public boolean isSuccessful(Job job, int statusCode) throws BindingException {
    return statusCode == 0;
  }

  @Override
  public Job preprocess(Job job, File workingDir) throws BindingException {
    return job;
  }

  @Override
  public Job postprocess(Job job, File workingDir) throws BindingException {
    return job;
  }

  @Override
  public String buildCommandLine(Job job) throws BindingException {
    return rabixCommandLineBuilder.buildCommandLine(job);
  }

  @Override
  public List<String> buildCommandLineParts(Job job) throws BindingException {
    return rabixCommandLineBuilder.buildCommandLineParts(job);
  }

  @Override
  public Set<FileValue> getInputFiles(Job job) throws BindingException {
    Set<FileValue> files = new HashSet<>();
    
    for (Entry<String, Object> inputEntry : job.getInputs().entrySet()) {
      if (inputEntry.getValue() instanceof String) {
        if (RabixHelper.isFile(inputEntry.getValue())) {
          String path = RabixHelper.getFilePath(inputEntry.getValue());
          files.add(new FileValue(0L, path, null, null, null));
        }
      }
    }
    return files;
  }

  @Override
  public Set<FileValue> getOutputFiles(Job job) throws BindingException {
    Set<FileValue> files = new HashSet<>();
    
    for (Entry<String, Object> inputEntry : job.getOutputs().entrySet()) {
      if (inputEntry.getValue() instanceof String) {
        if (RabixHelper.isFile(inputEntry.getValue())) {
          String path = RabixHelper.getFilePath(inputEntry.getValue());
          files.add(new FileValue(0L, path, null, null, null));
        }
      }
    }
    return files;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Job mapInputFilePaths(Job job, FileMapper fileMapper) throws BindingException {
    Map<String, Object> newInputs = (Map<String, Object>) CloneHelper.deepCopy(job.getInputs());
    
    for (Entry<String, Object> inputEntry : newInputs.entrySet()) {
      if (inputEntry.getValue() instanceof String) {
        if (RabixHelper.isFile(inputEntry.getValue())) {
          String path = RabixHelper.getFilePath(inputEntry.getValue());
          try {
            inputEntry.setValue(fileMapper.map(path));
          } catch (FileMappingException e) {
            throw new BindingException(e);
          }
        }
      }
    }
    return Job.cloneWithInputs(job, newInputs);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Job mapOutputFilePaths(Job job, FileMapper fileMapper) throws BindingException {
    Map<String, Object> newOutputs = (Map<String, Object>) CloneHelper.deepCopy(job.getOutputs());

    for (Entry<String, Object> inputEntry : newOutputs.entrySet()) {
      if (inputEntry.getValue() instanceof String) {
        if (RabixHelper.isFile(inputEntry.getValue())) {
          String path = RabixHelper.getFilePath(inputEntry.getValue());
          try {
            inputEntry.setValue(fileMapper.map(path));
          } catch (FileMappingException e) {
            throw new BindingException(e);
          }
        }
      }
    }
    return Job.cloneWithOutputs(job, newOutputs);
  }

  @Override
  public List<Requirement> getRequirements(Job job) throws BindingException {
    return Collections.<Requirement>emptyList();
  }

  @Override
  public List<Requirement> getHints(Job job) throws BindingException {
    return Collections.<Requirement>emptyList();
  }

  @Override
  public DAGNode translateToDAG(Job job) throws BindingException {
    return rabixTranslator.translateToGeneric(job);
  }

  @Override
  public void validate(Job job) throws BindingException {
    // TODO implement
  }

  @Override
  public ProtocolType getProtocolType() {
    return this.protocolType;
  }
  
  static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

}
