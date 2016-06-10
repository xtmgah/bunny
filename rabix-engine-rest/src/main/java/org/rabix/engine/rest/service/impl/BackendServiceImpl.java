package org.rabix.engine.rest.service.impl;

import java.util.List;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.rabix.db.DBException;
import org.rabix.engine.rest.backend.BackendDispatcher;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.backend.stub.BackendStubFactory;
import org.rabix.engine.rest.db.BackendRecord;
import org.rabix.engine.rest.db.BackendRecordRepository;
import org.rabix.engine.rest.service.BackendService;
import org.rabix.engine.rest.service.EngineRestServiceException;
import org.rabix.engine.rest.service.JobService;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.Backend.BackendType;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendRabbitMQ.BackendConfiguration;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportConfigRabbitMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BackendServiceImpl implements BackendService {

  private final static Logger logger = LoggerFactory.getLogger(BackendServiceImpl.class); 
  
  private final JobService jobService;
  private final Configuration configuration;
  private final BackendDispatcher backendDispatcher;
  private final BackendStubFactory backendStubFactory;
  private final BackendRecordRepository backendRecordRepository;
  
  @Inject
  public BackendServiceImpl(JobService jobService, BackendStubFactory backendStubFactory, BackendDispatcher backendDispatcher, Configuration configuration, BackendRecordRepository backendRecordRepository) {
    this.jobService = jobService;
    this.configuration = configuration;
    this.backendDispatcher = backendDispatcher;
    this.backendStubFactory = backendStubFactory;
    this.backendRecordRepository = backendRecordRepository;
  }
  
  @Override
  @Transactional
  public <T extends Backend> T create(T backend) throws EngineRestServiceException {
    backend = populate(backend);
    try {
      backendRecordRepository.insert(backend, true);
      
      BackendStub backendStub = backendStubFactory.create(jobService, backend);
      backendDispatcher.addBackendStub(backendStub);
      return backend;
    } catch (Exception e) {
      logger.error("Failed to create Backend " + backend, e);
      throw new EngineRestServiceException("Failed to create Backend " + backend, e);
    }
  }
  
  @Override
  @Transactional
  public void update(BackendRecord backendRecord) throws EngineRestServiceException {
    try {
      backendRecordRepository.update(backendRecord);
    } catch (DBException e) {
      logger.error("Failed to update BackendRecord " + backendRecord, e);
      throw new EngineRestServiceException("Failed to update BackendRecord " + backendRecord, e);
    }
  }
  
  @Override
  @Transactional
  public void updateHeartbeat(String id, Long heartbeat) throws EngineRestServiceException {
    try {
      backendRecordRepository.updateHeartbeat(id, heartbeat);
    } catch (DBException e) {
      logger.error("Failed to update heartbeat for " + id, e);
      throw new EngineRestServiceException("Failed to update heartbeat for " + id, e);
    }
  }
  
  private <T extends Backend> T populate(T backend) {
    backend.setId(generateUniqueBackendId());
    
    if (BackendType.RABBIT_MQ.equals(backend.getType())) {
      String backendExchange = TransportConfigRabbitMQ.getBackendExchange(configuration);
      String backendExchangeType = TransportConfigRabbitMQ.getBackendExchangeType(configuration);
      String backendReceiveRoutingKey = TransportConfigRabbitMQ.getBackendReceiveRoutingKey(configuration);
      
      backendExchange = backendExchange + "_" + backend.getId();
      BackendConfiguration backendConfiguration = new BackendConfiguration(backendExchange, backendExchangeType, backendReceiveRoutingKey);
      ((BackendRabbitMQ) backend).setBackendConfiguration(backendConfiguration);
    }
    return backend;
  }
  
  @Override
  @Transactional
  public List<BackendRecord> findActive() throws EngineRestServiceException {
    try {
      return backendRecordRepository.findActive();
    } catch (DBException e) {
      logger.error("Failed to find active backends", e);
      throw new EngineRestServiceException("Failed to find active backends", e);
    }
  }
  
  private String generateUniqueBackendId() {
    return UUID.randomUUID().toString();
  }

}
