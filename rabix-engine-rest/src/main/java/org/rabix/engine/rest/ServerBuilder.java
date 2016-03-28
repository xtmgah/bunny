package org.rabix.engine.rest;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.ws.rs.ApplicationPath;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.rabix.common.config.ConfigModule;
import org.rabix.engine.EngineModule;
import org.rabix.engine.rest.api.JobHTTPService;
import org.rabix.engine.rest.api.TaskHTTPService;
import org.rabix.engine.rest.api.impl.JobHTTPServiceImpl;
import org.rabix.engine.rest.api.impl.TaskHTTPServiceImpl;
import org.rabix.engine.rest.db.TaskDB;
import org.rabix.engine.rest.plugin.BackendPluginConfig;
import org.rabix.engine.rest.plugin.BackendPluginRegister;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.TaskService;
import org.rabix.engine.rest.service.impl.JobServiceImpl;
import org.rabix.engine.rest.service.impl.TaskServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.squarespace.jersey2.guice.BootstrapUtils;

public class ServerBuilder {

  private int port = 8081;

  public ServerBuilder() {
  }

  public ServerBuilder(int port) {
    this.port = port;
  }

  public Server build() {
    ServiceLocator locator = BootstrapUtils.newServiceLocator();
    
    File configDir = new File("config");
    BootstrapUtils.newInjector(locator, Arrays.asList(
        new ServletModule(), 
        new ConfigModule(configDir, null), 
        new EngineModule(), 
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(TaskDB.class).in(Scopes.SINGLETON);
            bind(TaskService.class).to(TaskServiceImpl.class).in(Scopes.SINGLETON);
            bind(JobService.class).to(JobServiceImpl.class).in(Scopes.SINGLETON);
            bind(BackendPluginRegister.class).in(Scopes.SINGLETON);
            bind(BackendPluginConfig.class).in(Scopes.SINGLETON);
            bind(JobHTTPService.class).to(JobHTTPServiceImpl.class);
            bind(TaskHTTPService.class).to(TaskHTTPServiceImpl.class).in(Scopes.SINGLETON);
          }
        }));

    BootstrapUtils.install(locator);

    Server server = new Server(port);

    ResourceConfig config = ResourceConfig.forApplication(new Application());

    ServletContainer servletContainer = new ServletContainer(config);

    ServletHolder sh = new ServletHolder(servletContainer);
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");

    FilterHolder filterHolder = new FilterHolder(GuiceFilter.class);
    context.addFilter(filterHolder, "/*", EnumSet.allOf(DispatcherType.class));

    context.addServlet(sh, "/*");
    
    
    ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setDirectoriesListed(true);
    resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
    resourceHandler.setResourceBase("./web");

    HandlerList handlers = new HandlerList();
    handlers.setHandlers(new Handler[] { resourceHandler, context });
    server.setHandler(handlers);
    
    server.setHandler(handlers);
    return server;
  }
  
  @ApplicationPath("/")
  public class Application extends ResourceConfig {
    
    public Application() {
      packages("org.rabix.engine.rest.api");
    }
  }
}
