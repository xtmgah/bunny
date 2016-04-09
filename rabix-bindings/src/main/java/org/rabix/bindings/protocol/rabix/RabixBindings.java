package org.rabix.bindings.protocol.rabix;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.visitor.BaseVisitor;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.helper.Draft2JobHelper;
import org.rabix.bindings.protocol.rabix.bean.RabixJob;
import org.rabix.bindings.protocol.rabix.bean.RabixJobApp;
import org.rabix.common.helper.JSONHelper;

public class RabixBindings implements Bindings {

  private ProtocolType protocolType;
  private RabixCommandLineBuilder rabixCommandLineBuilder;
  private RabixTranslator rabixTranslator;

  public RabixBindings() throws BindingException {
    this.protocolType = ProtocolType.RABIX;
    RabixCommandLineBuilder rabixCommandLineBuilder = new RabixCommandLineBuilder();
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
    RabixJob rabixJob = rabixTranslator.translateToRabixJob(job);
    for (Object input : rabixJob.getInputs().values()) {

    }
    return null;
  }

  @Override
  public Set<FileValue> getOutputFiles(Job job) throws BindingException {
    RabixJob rabixJob = rabixTranslator.translateToRabixJob(job);
    for (Object input : rabixJob.getInputs().values()) {

    }
    return null;
  }

  @Override
  public Job mapInputFilePaths(Job job, FileMapper fileMapper) throws BindingException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Job mapOutputFilePaths(Job job, FileMapper fileMapper) throws BindingException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Requirement> getRequirements(Job job) throws BindingException {
    return null;
  }

  @Override
  public List<Requirement> getHints(Job job) throws BindingException {
    return null;
  }

  @Override
  public DAGNode translateToDAG(Job job) throws BindingException {
    return rabixTranslator.translateToGeneric(job);
  }

  @Override
  public void validate(Job job) throws BindingException {
    // TODO Auto-generated method stub

  }

  @Override
  public ProtocolType getProtocolType() {
    return this.protocolType;
  }
  
  static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  public static void main(String[] args) throws ParseException, BindingException, IOException {
    RabixBindings rabixBindings = new RabixBindings();
    String app = rabixBindings.loadApp("file://Users/Sinisa/Desktop/Bunny-test/grep.rbx");
    Object appObj = rabixBindings.loadAppObject("file://Users/Sinisa/Desktop/Bunny-test/grep.rbx");
    File inputsFile = new File("/Users/Sinisa/Desktop/Bunny-test/grep-inputs.json");
    String inputsText = readFile(inputsFile.getAbsolutePath(), Charset.defaultCharset());
    Map<String, Object> inputs = JSONHelper.readMap(JSONHelper.transformToJSON(inputsText));
    
    
    
  }

}
