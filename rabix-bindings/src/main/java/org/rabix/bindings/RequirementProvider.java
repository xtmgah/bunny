package org.rabix.bindings;

import java.util.List;

import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.Requirement;

public interface RequirementProvider {

  Executable populateResources(Executable executable) throws BindingException;
  
  DockerContainerRequirement getDockerRequirement(Executable executable) throws BindingException;
  
  EnvironmentVariableRequirement getEnvironmentVariableRequirement(Executable executable) throws BindingException;
  
  FileRequirement getFileRequirement(Executable executable) throws BindingException;
  
  List<Requirement> getRequirements(Executable executable) throws BindingException;
  
  List<Requirement> getHints(Executable executable) throws BindingException;
}
