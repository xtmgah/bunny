package org.rabix.engine.rest.service.impl;

import java.util.UUID;

import org.rabix.engine.rest.backend.BackendDispatcher;
import org.rabix.engine.rest.backend.impl.BackendMQ;
import org.rabix.engine.rest.db.BackendDB;
import org.rabix.engine.rest.model.Backend;
import org.rabix.engine.rest.service.BackendService;
import org.rabix.engine.rest.service.JobService;

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
  public Backend create(Backend backend) {
    String id = UUID.randomUUID().toString();
    backend = Backend.cloneWithID(backend, id);
    backendDB.add(backend);
    
    BackendMQ backendMQ = new BackendMQ(jobService, backend);
    backendMQ.startConsumer();
    backendDispatcher.addBackendMQ(backendMQ);
    return backend;
  }
  
}
