package org.rabix.engine.rest.service.impl;

import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.rabix.engine.rest.backend.BackendDispatcher;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.backend.stub.BackendStubFactory;
import org.rabix.engine.rest.db.BackendDB;
import org.rabix.engine.rest.service.BackendService;
import org.rabix.engine.rest.service.JobService;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.Backend.BackendType;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendRabbitMQ.BackendConfiguration;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportConfigRabbitMQ;

import com.google.inject.Inject;

public class BackendServiceImpl implements BackendService {

  private final BackendDB backendDB;
  private final JobService jobService;
  private final Configuration configuration;
  private final BackendDispatcher backendDispatcher;
  private final BackendStubFactory backendStubFactory;
  
  @Inject
  public BackendServiceImpl(JobService jobService, BackendStubFactory backendStubFactory, BackendDB backendDB, BackendDispatcher backendDispatcher, Configuration configuration) {
    this.backendDB = backendDB;
    this.jobService = jobService;
    this.configuration = configuration;
    this.backendDispatcher = backendDispatcher;
    this.backendStubFactory = backendStubFactory;
  }
  
  @Override
  public <T extends Backend> T create(T backend) throws TransportPluginException {
    backend = populate(backend);
    backendDB.add(backend);
    
    BackendStub backendStub = backendStubFactory.create(jobService, backend);
    backendDispatcher.addBackendStub(backendStub);
    return backend;
  }
  
  private <T extends Backend> T populate(T backend) {
    backend.setId(generateUniqueBackendId());
    
    if (BackendType.RABBIT_MQ.equals(backend.getType())) {
      String backendExchange = TransportConfigRabbitMQ.getBackendExchange(configuration);
      String backendExchangeType = TransportConfigRabbitMQ.getBackendExchangeType(configuration);
      String backendReceiveRoutingKey = TransportConfigRabbitMQ.getBackendReceiveRoutingKey(configuration);
      
      BackendConfiguration backendConfiguration = new BackendConfiguration(backendExchange, backendExchangeType, backendReceiveRoutingKey);
      ((BackendRabbitMQ) backend).setBackendConfiguration(backendConfiguration);
    }
    return backend;
  }
  
  private String generateUniqueBackendId() {
    return UUID.randomUUID().toString();
  }
  
}
