package org.rabix.executor;

import org.rabix.executor.service.JobReceiver;
import org.rabix.executor.service.impl.receiver.JobReceiverLocal;
import org.rabix.executor.transport.TransportStub;
import org.rabix.executor.transport.impl.local.TransportStubLocal;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class ExecutorTransportModuleLocal extends AbstractModule {

  @Override
  protected void configure() {
    bind(TransportStub.class).to(TransportStubLocal.class);
    bind(JobReceiver.class).to(JobReceiverLocal.class).in(Scopes.SINGLETON);
  }

}
