package org.rabix.engine.rest.backend.stub;

import org.apache.commons.configuration.Configuration;
import org.rabix.engine.rest.backend.stub.impl.BackendStubActiveMQ;
import org.rabix.engine.rest.backend.stub.impl.BackendStubLocal;
import org.rabix.engine.rest.backend.stub.impl.BackendStubRabbitMQ;
import org.rabix.engine.rest.service.BackendService;
import org.rabix.engine.rest.service.JobService;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendActiveMQ;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.mechanism.TransportPluginException;

import com.google.inject.Inject;

public class BackendStubFactory {

  private final Configuration configuration;
  private final BackendService backendService;

  @Inject
  public BackendStubFactory(Configuration configuration, BackendService backendService) {
    this.configuration = configuration;
    this.backendService = backendService;
  }

  public <T extends Backend> BackendStub create(JobService jobService, T backend) throws TransportPluginException {
    switch (backend.getType()) {
    case ACTIVE_MQ:
      return new BackendStubActiveMQ(jobService, configuration, backendService, (BackendActiveMQ) backend);
    case LOCAL:
      return new BackendStubLocal(jobService, configuration, (BackendLocal) backend, backendService);
    case RABBIT_MQ:
      return new BackendStubRabbitMQ(jobService, (BackendRabbitMQ) backend, backendService, configuration);
    default:
      break;
    }
    throw new TransportPluginException("There is no Backend stub for " + backend);
  }

}
