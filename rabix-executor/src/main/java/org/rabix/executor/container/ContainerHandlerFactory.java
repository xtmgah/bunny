package org.rabix.executor.container;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.LocalContainerRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.executor.container.impl.DockerContainerHandler;
import org.rabix.executor.container.impl.DockerContainerHandler.DockerClientLockDecorator;
import org.rabix.executor.container.impl.LocalContainerHandler;
import org.rabix.executor.status.ExecutorStatusCallback;

public class ContainerHandlerFactory {

  public static ContainerHandler create(Job job, Requirement requirement, DockerClientLockDecorator dockerClient, ExecutorStatusCallback statusCallback, Configuration configuration) throws ContainerException {
    if (requirement instanceof DockerContainerRequirement) {
      return new DockerContainerHandler(job, (DockerContainerRequirement) requirement, configuration, statusCallback, dockerClient);
    }
    if (requirement instanceof LocalContainerRequirement) {
      return new LocalContainerHandler(job, configuration);
    }
    return null;
  }
  
}
