package org.rabix.engine.rest.backend.stub;

import org.rabix.engine.rest.backend.stub.impl.BackendStubLocal;
import org.rabix.engine.rest.backend.stub.impl.BackendStubRabbitMQ;
import org.rabix.engine.rest.backend.stub.impl.BackendStubActiveMQ;
import org.rabix.engine.rest.service.JobService;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendActiveMQ;

public class BackendStubFactory {

  public static <T extends Backend> BackendStub createStub(JobService jobService, T backend) {
    switch (backend.getType()) {
    case ACTIVE_MQ:
      return new BackendStubActiveMQ(jobService, (BackendActiveMQ) backend);
    case LOCAL:
      return new BackendStubLocal(jobService, (BackendLocal) backend);
    case RABBIT_MQ:
      return new BackendStubRabbitMQ(jobService, (BackendRabbitMQ) backend);
    default:
      break;
    }
    return null;
  }

}
