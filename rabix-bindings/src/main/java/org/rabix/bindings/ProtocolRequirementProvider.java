package org.rabix.bindings;

import java.util.List;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.model.requirement.ResourceRequirement;

public interface ProtocolRequirementProvider {
  
  ResourceRequirement getResourceRequirement(Job job) throws BindingException;

  List<Requirement> getRequirements(Job job) throws BindingException;
  
  List<Requirement> getHints(Job job) throws BindingException;
}
