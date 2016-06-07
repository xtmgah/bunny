package org.rabix.engine;

import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.dispatcher.EventDispatcherFactory;
import org.rabix.engine.processor.handler.HandlerFactory;
import org.rabix.engine.processor.handler.impl.ContextStatusEventHandler;
import org.rabix.engine.processor.handler.impl.InitEventHandler;
import org.rabix.engine.processor.handler.impl.InputEventHandler;
import org.rabix.engine.processor.handler.impl.JobStatusEventHandler;
import org.rabix.engine.processor.handler.impl.OutputEventHandler;
import org.rabix.engine.processor.impl.EventProcessorImpl;
import org.rabix.engine.service.ApplicationPayloadService;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.DAGNodeGraphService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.service.scatter.ScatterService;
import org.rabix.engine.service.scatter.strategy.ScatterStrategyHandlerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class EngineModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DAGNodeGraphService.class).in(Scopes.SINGLETON);
    bind(ApplicationPayloadService.class).in(Scopes.SINGLETON);
    
    bind(JobRecordService.class).in(Scopes.SINGLETON);
    bind(VariableRecordService.class).in(Scopes.SINGLETON);
    bind(LinkRecordService.class).in(Scopes.SINGLETON);
    bind(ContextRecordService.class).in(Scopes.SINGLETON);

    bind(ScatterService.class).in(Scopes.SINGLETON);
    bind(ScatterStrategyHandlerFactory.class).in(Scopes.SINGLETON);
    bind(InitEventHandler.class).in(Scopes.SINGLETON);
    bind(InputEventHandler.class).in(Scopes.SINGLETON);
    bind(OutputEventHandler.class).in(Scopes.SINGLETON);
    bind(JobStatusEventHandler.class).in(Scopes.SINGLETON);
    bind(ContextStatusEventHandler.class).in(Scopes.SINGLETON);
    
    bind(HandlerFactory.class).in(Scopes.SINGLETON);
    bind(EventDispatcherFactory.class).in(Scopes.SINGLETON);
    bind(EventProcessor.class).to(EventProcessorImpl.class).in(Scopes.SINGLETON);
  }

}
