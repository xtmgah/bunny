package org.rabix.executor.rest;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.ws.rs.ApplicationPath;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.rabix.common.config.ConfigModule;
import org.rabix.executor.ExecutorModule;
import org.rabix.executor.rest.api.ExecutorHTTPService;
import org.rabix.executor.rest.api.impl.ExecutorHTTPServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.squarespace.jersey2.guice.BootstrapUtils;

public class ServerBuilder {

  private int port = 8080;

  public ServerBuilder() {
  }

  public ServerBuilder(int port) {
    this.port = port;
  }

  public Server build() {
    ServiceLocator locator = BootstrapUtils.newServiceLocator();
    
    File configDir = new File("config");
    
    ConfigModule configModule = new ConfigModule(configDir, null);
    BootstrapUtils.newInjector(locator, Arrays.asList(new ServletModule(), new ExecutorModule(configModule), new AbstractModule() {
      @Override
      protected void configure() {
        bind(ExecutorHTTPService.class).to(ExecutorHTTPServiceImpl.class).in(Scopes.SINGLETON);;
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
    server.setHandler(context);
    return server;
  }
  
  @ApplicationPath("/")
  public class Application extends ResourceConfig {
    
    public Application() {
      packages("org.rabix.executor.rest.api");
    }
  }
}
