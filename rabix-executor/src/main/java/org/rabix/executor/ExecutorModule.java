package org.rabix.executor;

import org.rabix.common.config.ConfigModule;
import org.rabix.executor.execution.JobHandlerCommandDispatcher;
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.handler.JobHandlerFactory;
import org.rabix.executor.handler.impl.JobHandlerImpl;
import org.rabix.executor.service.DownloadFileService;
import org.rabix.executor.service.ExecutorService;
import org.rabix.executor.service.JobDataService;
import org.rabix.executor.service.impl.DownloadServiceImpl;
import org.rabix.executor.service.impl.ExecutorServiceImpl;
import org.rabix.executor.service.impl.JobDataServiceImpl;

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
    install(new FactoryModuleBuilder().implement(JobHandler.class, JobHandlerImpl.class).build(JobHandlerFactory.class));

    bind(DownloadFileService.class).to(DownloadServiceImpl.class).in(Scopes.SINGLETON);

    bind(JobDataService.class).to(JobDataServiceImpl.class).in(Scopes.SINGLETON);
    bind(JobHandlerCommandDispatcher.class).in(Scopes.SINGLETON);

    bind(ExecutorService.class).to(ExecutorServiceImpl.class).in(Scopes.SINGLETON);
  }

}
