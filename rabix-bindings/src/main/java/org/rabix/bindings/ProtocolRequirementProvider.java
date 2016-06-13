package org.rabix.bindings;

import java.util.List;

import org.rabix.bindings.model.Cpu;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Memory;
import org.rabix.bindings.model.requirement.Requirement;

public interface ProtocolRequirementProvider {

  List<Requirement> getRequirements(Job job) throws BindingException;
  
  List<Requirement> getHints(Job job) throws BindingException;

  Cpu getCPU(Job job) throws BindingException;

  Memory getMemory(Job job) throws BindingException;
}
