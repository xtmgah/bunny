package org.rabix.executor.transport.impl;

import org.rabix.executor.transport.TransportStub;

public class TransportStubRabbitMQ implements TransportStub {

  @Override
  public <T> ResultPair<T> send(String destinationQueue, T entity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> ResultPair<T> receive(String sourceQueue, Class<T> clazz) {
    // TODO Auto-generated method stub
    return null;
  }

}
