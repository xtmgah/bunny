package org.rabix.executor;

import org.rabix.executor.service.JobReceiver;
import org.rabix.executor.service.impl.receiver.JobReceiverMQ;
import org.rabix.executor.transport.TransportStub;
import org.rabix.executor.transport.impl.TransportStubMQ;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class ExecutorTransportModuleMQ extends AbstractModule {

  @Override
  protected void configure() {
    bind(TransportStub.class).to(TransportStubMQ.class);
    bind(JobReceiver.class).to(JobReceiverMQ.class).in(Scopes.SINGLETON);    
  }

}
