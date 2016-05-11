package org.rabix.engine.rest.service.impl;

import java.util.UUID;

import org.rabix.engine.rest.backend.BackendDispatcher;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.backend.stub.BackendStubFactory;
import org.rabix.engine.rest.db.BackendDB;
import org.rabix.engine.rest.service.BackendService;
import org.rabix.engine.rest.service.JobService;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendRabbitMQ.BackendConfiguration;

import com.google.inject.Inject;

public class BackendServiceImpl implements BackendService {

  private final BackendDB backendDB;
  private final JobService jobService;
  private final BackendDispatcher backendDispatcher;
  
  @Inject
  public BackendServiceImpl(JobService jobService, BackendDB backendDB, BackendDispatcher backendDispatcher) {
    this.backendDB = backendDB;
    this.jobService = jobService;
    this.backendDispatcher = backendDispatcher;
  }
  
  @Override
  public <T extends Backend> T create(T backend) {
    backend = populate(backend);
    backendDB.add(backend);
    
    BackendStub backendStub = BackendStubFactory.createStub(jobService, backend);
    backendStub.start();
    backendDispatcher.addBackendStub(backendStub);
    return backend;
  }
  
  private <T extends Backend> T populate(T backend) {
    backend.setId(UUID.randomUUID().toString());
    switch (backend.getType()) {
    case RABBIT_MQ:
      BackendConfiguration backendConfiguration = new BackendConfiguration("backend_exchange_10", "direct", "receive_routing_key");
      ((BackendRabbitMQ) backend).setBackendConfiguration(backendConfiguration);
      break;
    default:
      break;
    }
    return backend;
  }
  
}
