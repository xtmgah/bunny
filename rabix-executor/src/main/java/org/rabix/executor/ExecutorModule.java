package org.rabix.executor;

import org.rabix.common.config.ConfigModule;
import org.rabix.executor.execution.ExecutableHandlerCommandDispatcher;
import org.rabix.executor.handler.ExecutableHandler;
import org.rabix.executor.handler.ExecutableHandlerFactory;
import org.rabix.executor.handler.impl.ExecutableHandlerImpl;
import org.rabix.executor.service.DownloadFileService;
import org.rabix.executor.service.ExecutableDataService;
import org.rabix.executor.service.ExecutorService;
import org.rabix.executor.service.impl.DownloadServiceImpl;
import org.rabix.executor.service.impl.ExecutableDataServiceImpl;
import org.rabix.executor.service.impl.ExecutorServiceImpl;

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
    install(new FactoryModuleBuilder().implement(ExecutableHandler.class, ExecutableHandlerImpl.class).build(ExecutableHandlerFactory.class));

    bind(DownloadFileService.class).to(DownloadServiceImpl.class).in(Scopes.SINGLETON);

    bind(ExecutableDataService.class).to(ExecutableDataServiceImpl.class).in(Scopes.SINGLETON);
    bind(ExecutableHandlerCommandDispatcher.class).in(Scopes.SINGLETON);

    bind(ExecutorService.class).to(ExecutorServiceImpl.class).in(Scopes.SINGLETON);
  }

}
