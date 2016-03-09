package org.rabix.executor.container;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.LocalContainerRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.executor.container.docker.DockerContainerHandler;
import org.rabix.executor.container.local.LocalContainerHandler;

public class ContainerHandlerFactory {

  public static ContainerHandler create(Executable executable, Requirement requirement, Configuration configuration) {
    if (requirement instanceof DockerContainerRequirement) {
      return new DockerContainerHandler(executable, (DockerContainerRequirement) requirement, configuration);
    }
    if (requirement instanceof LocalContainerRequirement) {
      return new LocalContainerHandler(executable, configuration);
    }
    return null;
  }
  
}
