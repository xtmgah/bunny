package org.rabix.executor;

import org.rabix.common.config.ConfigModule;
import org.rabix.common.retry.RetryInterceptorModule;
import org.rabix.executor.container.impl.DockerContainerHandler.DockerClientLockDecorator;
import org.rabix.executor.execution.JobHandlerCommandDispatcher;
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.handler.JobHandlerFactory;
import org.rabix.executor.handler.impl.JobHandlerImpl;
import org.rabix.executor.service.BasicMemoizationService;
import org.rabix.executor.service.ExecutorService;
import org.rabix.executor.service.JobDataService;
import org.rabix.executor.service.JobFitter;
import org.rabix.executor.service.impl.BasicMemoizationServiceImpl;
import org.rabix.executor.service.impl.ExecutorServiceImpl;
import org.rabix.executor.service.impl.JobDataServiceImpl;
import org.rabix.executor.service.impl.JobFitterImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ExecutorModule extends AbstractModule {

  private final ConfigModule configModule;

  public ExecutorModule(ConfigModule configModule) {
    this.configModule = configModule;
  }

  @Override
  protected void configure() {
    install(configModule);
    install(new RetryInterceptorModule());
    install(new FactoryModuleBuilder().implement(JobHandler.class, JobHandlerImpl.class).build(JobHandlerFactory.class));

    bind(DockerClientLockDecorator.class).in(Scopes.SINGLETON);

    bind(JobFitter.class).to(JobFitterImpl.class).in(Scopes.SINGLETON);
    bind(JobDataService.class).to(JobDataServiceImpl.class).in(Scopes.SINGLETON);
    bind(JobHandlerCommandDispatcher.class).in(Scopes.SINGLETON);

    bind(ExecutorService.class).to(ExecutorServiceImpl.class).in(Scopes.SINGLETON);
    bind(BasicMemoizationService.class).to(BasicMemoizationServiceImpl.class).in(Scopes.SINGLETON);
  }

}
