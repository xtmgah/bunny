package org.rabix.bindings.protocol.zero;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
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
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.Cpu;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Memory;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.protocol.zero.bean.ZeroJobApp;
import org.rabix.bindings.protocol.zero.helper.ZeroHelper;
import org.rabix.common.helper.CloneHelper;

public class ZeroBindings implements Bindings {

  private ProtocolType protocolType;
  private ZeroCommandLineBuilder rabixCommandLineBuilder = new ZeroCommandLineBuilder();;
  private final ZeroTranslator rabixTranslator = new ZeroTranslator();;

  public ZeroBindings() throws BindingException {
    this.protocolType = ProtocolType.ZERO;
  }

  @Override
  public String loadApp(String appURI) throws BindingException {
    return ZeroAppProcessor.loadApp(appURI);
  }

  @Override
  public Application loadAppObject(String appURI) throws BindingException {
    String app = loadApp(appURI);
    return ZeroAppProcessor.loadAppObject(appURI, app);
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
  @SuppressWarnings("unchecked")
  public Job postprocess(Job job, File workingDir) throws BindingException {
    String appString = loadApp(job.getApp());
    ZeroJobApp app = (ZeroJobApp) ZeroAppProcessor.loadAppObject(job.getId(), appString);
    String outputFile = app.getOutputs().get(0).getId();
    String fileName = ((Map<String, String>) app.getOutputs().get(0).getSchema()).get("glob");
    Map<String, Object> outputs = new HashMap<String, Object>();
    outputs.put(outputFile, ZeroHelper.getOutputPath(fileName, workingDir.getPath()));
    return Job.cloneWithOutputs(job, outputs);
  }

  @Override
  public String buildCommandLine(Job job) throws BindingException {
    String app = loadApp(job.getApp());
    return rabixCommandLineBuilder.buildCommandLine(job, app);
  }

  @Override
  public List<String> buildCommandLineParts(Job job) throws BindingException {
    String app = loadApp(job.getApp());
    return rabixCommandLineBuilder.buildCommandLineParts(job, app);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<FileValue> getInputFiles(Job job) throws BindingException {
    Set<FileValue> files = new HashSet<>();
    for (Entry<String, Object> inputEntry : job.getInputs().entrySet()) {
      if (inputEntry.getValue() instanceof Map<?, ?>) {
        Map<String, Object> map = (Map<String, Object>) inputEntry.getValue();
        if (map.containsKey("class") && map.get("class").equals("File")) {
          files.add(new FileValue(0L, (String) map.get("path"), null, null, null));
        }
      }
    }
    return files;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<FileValue> getOutputFiles(Job job) throws BindingException {
    Set<FileValue> files = new HashSet<>();
    for (Entry<String, Object> inputEntry : job.getOutputs().entrySet()) {
      if (inputEntry.getValue() instanceof String) {
        if (inputEntry.getValue() instanceof Map<?, ?>) {
          Map<String, Object> map = (Map<String, Object>) inputEntry.getValue();
          if (map.containsKey("class") && map.get("class").equals("File")) {
            files.add(new FileValue(0L, (String) map.get("path"), null, null, null));
          }
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
      if (inputEntry.getValue() instanceof Map<?, ?>) {
        Map<String, Object> map = (Map<String, Object>) inputEntry.getValue();
        if (map.containsKey("class") && map.get("class").equals("File")) {
          try {
            map.put("path", fileMapper.map((String) map.get("path")));
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
      if (inputEntry.getValue() instanceof Map<?, ?>) {
        Map<String, Object> map = (Map<String, Object>) inputEntry.getValue();
        if (map.containsKey("class") && map.get("class").equals("File")) {
          try {
            map.put("path", fileMapper.map((String) map.get("path")));
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
    return Collections.<Requirement> emptyList();
  }

  @Override
  public List<Requirement> getHints(Job job) throws BindingException {
    return Collections.<Requirement> emptyList();
  }

  @Override
  public DAGNode translateToDAG(Job job) throws BindingException {
    return rabixTranslator.translateToGeneric(job);
  }

  @Override
  public void validate(Job job) throws BindingException {
    String app = loadApp(job.getApp());
    String[] lines = app.split("\\n");
    if(lines.length < 2 || !lines[0].equals("#zero:SimpleZeroTool")) {
      throw new BindingException("Invalid RabixApp");
    }
  }

  @Override
  public ProtocolType getProtocolType() {
    return this.protocolType;
  }

  @Override
  public Cpu getCPU(Job job) throws BindingException {
    return null;
  }

  @Override
  public Memory getMemory(Job job) throws BindingException {
    return null;
  }

}
