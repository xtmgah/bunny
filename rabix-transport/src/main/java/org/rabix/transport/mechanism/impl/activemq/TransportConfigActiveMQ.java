package org.rabix.transport.mechanism.impl.activemq;

import org.apache.commons.configuration.Configuration;

public class TransportConfigActiveMQ {

  public static String getBroker(Configuration configuration) {
    return configuration.getString("activemq.broker");
  }
  
  public static String getSendToBackendQueue(Configuration configuration) {
    return configuration.getString("activemq.toBackendQueue");
  }
  
  public static String getReceiveFromBackendQueue(Configuration configuration) {
    return configuration.getString("activemq.fromBackendQueue");
  }
  
  public static String getReceiveHeartbeatFromBackendQueue(Configuration configuration) {
    return configuration.getString("activemq.fromBackendHeartbeatQueue");
  }
}
