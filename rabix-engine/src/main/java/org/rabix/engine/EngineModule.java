package org.rabix.engine;

import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.dispatcher.EventDispatcherFactory;
import org.rabix.engine.processor.handler.HandlerFactory;
import org.rabix.engine.processor.handler.impl.ContextStatusEventHandler;
import org.rabix.engine.processor.handler.impl.InitEventHandler;
import org.rabix.engine.processor.handler.impl.InputEventHandler;
import org.rabix.engine.processor.handler.impl.JobStatusEventHandler;
import org.rabix.engine.processor.handler.impl.OutputEventHandler;
import org.rabix.engine.processor.impl.EventProcessorImpl;
import org.rabix.engine.service.ContextService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.LinkService;
import org.rabix.engine.service.VariableService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class EngineModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DAGNodeDB.class).in(Scopes.SINGLETON);
    
    bind(JobService.class).in(Scopes.SINGLETON);
    bind(VariableService.class).in(Scopes.SINGLETON);
    bind(LinkService.class).in(Scopes.SINGLETON);
    bind(ContextService.class).in(Scopes.SINGLETON);

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
