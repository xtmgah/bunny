package org.rabix.transport.backend;

import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendRabbitMQ.BackendConfiguration;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportConfigRabbitMQ;

import com.google.inject.Inject;

public class BackendPopulator {

  private final Configuration configuration;

  @Inject
  public BackendPopulator(Configuration configuration) {
    this.configuration = configuration;
  }

  public <T extends Backend> T populate(T backend) {
    if (backend.getId() == null) {
      backend.setId(generateUniqueBackendId());
    }
    switch (backend.getType()) {
    case RABBIT_MQ:
      if (((BackendRabbitMQ) backend).getBackendConfiguration() == null) {
        String backendExchange = TransportConfigRabbitMQ.getBackendExchange(configuration);
        String backendExchangeType = TransportConfigRabbitMQ.getBackendExchangeType(configuration);
        String backendReceiveRoutingKey = TransportConfigRabbitMQ.getBackendReceiveRoutingKey(configuration);
        Long heartbeatPeriodMills = TransportConfigRabbitMQ.getBackendHeartbeatTimeMills(configuration);

        backendExchange = backendExchange + "_" + backend.getId();
        BackendConfiguration backendConfiguration = new BackendConfiguration(backendExchange, backendExchangeType, backendReceiveRoutingKey, heartbeatPeriodMills);
        ((BackendRabbitMQ) backend).setBackendConfiguration(backendConfiguration);
        return backend;
      }
      break;
    case ACTIVE_MQ:
      break;
    case LOCAL:
      break;
    default:
      break;
    }
    return backend;
  }
  
  private String generateUniqueBackendId() {
    return UUID.randomUUID().toString();
  }

}
