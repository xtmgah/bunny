package org.rabix.engine.rest.backend.stub;

import org.rabix.engine.rest.backend.Backend;
import org.rabix.engine.rest.backend.impl.BackendLocal;
import org.rabix.engine.rest.backend.impl.BackendMQ;
import org.rabix.engine.rest.backend.stub.impl.BackendStubLocal;
import org.rabix.engine.rest.backend.stub.impl.BackendStubMQ;
import org.rabix.engine.rest.service.JobService;

public class BackendStubFactory {

  public static <T extends Backend> BackendStub createStub(JobService jobService, T backend) {
    switch (backend.getType()) {
    case MQ:
      return new BackendStubMQ(jobService, (BackendMQ) backend);
    case LOCAL:
      return new BackendStubLocal(jobService, (BackendLocal) backend);
    default:
      break;
    }
    return null;
  }

}
