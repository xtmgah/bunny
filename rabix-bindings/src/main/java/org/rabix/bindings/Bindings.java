package org.rabix.bindings;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.model.requirement.Requirement;

public interface Bindings {

  String loadApp(String appURI) throws BindingException;

  boolean canExecute(Job job) throws BindingException;
  
  boolean isSuccessful(Job job, int statusCode) throws BindingException;

  Job preprocess(Job job, File workingDir) throws BindingException;
  
  Job postprocess(Job job, File workingDir) throws BindingException;

  String buildCommandLine(Job job) throws BindingException;

  List<String> buildCommandLineParts(Job job) throws BindingException;

  Set<FileValue> getInputFiles(Job job) throws BindingException;

  Set<FileValue> getOutputFiles(Job job) throws BindingException;
  
  Job mapInputFilePaths(Job job, FileMapper fileMapper) throws BindingException;

  Job mapOutputFilePaths(Job job, FileMapper fileMapper) throws BindingException;

  Job populateResources(Job job) throws BindingException;
  
  List<Requirement> getRequirements(Job job) throws BindingException;

  List<Requirement> getHints(Job job) throws BindingException;
  
  DAGNode translateToDAG(Job job) throws BindingException;

  void validate(Job job) throws BindingException;
  
  ProtocolType getProtocolType();
  
}
