package org.rabix.bindings;

import java.util.List;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.Requirement;

public interface RequirementProvider {

  Job populateResources(Job job) throws BindingException;
  
  DockerContainerRequirement getDockerRequirement(Job job) throws BindingException;
  
  EnvironmentVariableRequirement getEnvironmentVariableRequirement(Job job) throws BindingException;
  
  FileRequirement getFileRequirement(Job job) throws BindingException;
  
  List<Requirement> getRequirements(Job job) throws BindingException;
  
  List<Requirement> getHints(Job job) throws BindingException;
}
