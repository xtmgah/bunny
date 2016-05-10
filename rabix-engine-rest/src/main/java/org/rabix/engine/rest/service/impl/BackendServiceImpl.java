package org.rabix.engine.rest.service.impl;

import java.util.UUID;

import org.rabix.engine.rest.backend.BackendDispatcher;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.backend.stub.BackendStubFactory;
import org.rabix.engine.rest.db.BackendDB;
import org.rabix.engine.rest.service.BackendService;
import org.rabix.engine.rest.service.JobService;
import org.rabix.transport.backend.Backend;

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
    backend.setId(UUID.randomUUID().toString());
    backendDB.add(backend);
    
    BackendStub backendStub = BackendStubFactory.createStub(jobService, backend);
    backendStub.start();
    backendDispatcher.addBackendStub(backendStub);
    return backend;
  }
  
}
