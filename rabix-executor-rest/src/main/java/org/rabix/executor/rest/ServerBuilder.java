package org.rabix.executor.rest;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.Configuration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.rabix.common.config.ConfigModule;
import org.rabix.executor.ExecutorModule;
import org.rabix.executor.rest.api.ExecutorHTTPService;
import org.rabix.executor.rest.api.impl.ExecutorHTTPServiceImpl;
import org.rabix.executor.service.ExecutorService;
import org.rabix.executor.transport.TransportQueueConfig;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendActiveMQ;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.squarespace.jersey2.guice.BootstrapUtils;

public class ServerBuilder {

  private final static String EXECUTOR_PORT_KEY = "executor.port";
  
  private File configDir;
  
  public ServerBuilder(File configDir) {
    this.configDir = configDir;
  }

  public Server build() throws ExecutorException {
    ServiceLocator locator = BootstrapUtils.newServiceLocator();

    ConfigModule configModule = new ConfigModule(configDir, null);
    Injector injector = BootstrapUtils.newInjector(locator,
        Arrays.asList(
            new ServletModule(), 
            new ExecutorModule(configModule), 
            new AbstractModule() {
              @Override
              protected void configure() {
                bind(ExecutorHTTPService.class).to(ExecutorHTTPServiceImpl.class).in(Scopes.SINGLETON);
                bind(TransportQueueConfig.class).in(Scopes.SINGLETON);
                bind(BackendRegister.class).in(Scopes.SINGLETON);
              }
        }));

    BootstrapUtils.install(locator);

    Configuration configuration = injector.getInstance(Configuration.class);
    
    int enginePort = configuration.getInt(EXECUTOR_PORT_KEY);
    Server server = new Server(enginePort);
    
    ResourceConfig config = ResourceConfig.forApplication(new Application());

    ServletContainer servletContainer = new ServletContainer(config);

    ServletHolder sh = new ServletHolder(servletContainer);
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");

    FilterHolder filterHolder = new FilterHolder(GuiceFilter.class);
    context.addFilter(filterHolder, "/*", EnumSet.allOf(DispatcherType.class));

    context.addServlet(sh, "/*");
    server.setHandler(context);
    
    BackendRegister backendRegister = injector.getInstance(BackendRegister.class);
    Backend backend = backendRegister.start();
    
    ExecutorService executorService = injector.getInstance(ExecutorService.class);
    executorService.initialize(backend);
    return server;
  }

  @ApplicationPath("/")
  public class Application extends ResourceConfig {

    public Application() {
      packages("org.rabix.executor.rest.api");
    }
  }

  public static class BackendRegister {

    private Configuration configuration;
    private TransportQueueConfig transportQueueConfig;

    @Inject
    public BackendRegister(Configuration configuration, TransportQueueConfig transportQueueConfig) {
      this.transportQueueConfig = transportQueueConfig;
      this.configuration = configuration;
    }
    
    public Backend start() throws ExecutorException {
      try {
        return registerBackend();
      } catch (Exception e) {
        throw new ExecutorException("Failed to register executor to the Engine", e);
      }
    }

    private BackendActiveMQ registerBackend() {
      String engineHost = configuration.getString("engine.url");
      Integer enginePort = configuration.getInteger("engine.port", null);

      Client client = ClientBuilder.newClient(new ClientConfig().register(LoggingFilter.class));
      WebTarget webTarget = client.target(engineHost + ":" + enginePort + "/v0/engine/backends");

      BackendActiveMQ backend = new BackendActiveMQ(null, transportQueueConfig.getBroker(), transportQueueConfig.getToBackendQueue(), transportQueueConfig.getFromBackendQueue(), transportQueueConfig.getFromBackendHeartbeatQueue());
      Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
      Response response = invocationBuilder.post(Entity.entity(backend, MediaType.APPLICATION_JSON));
      return response.readEntity(BackendActiveMQ.class);
    }
    
  }
}
