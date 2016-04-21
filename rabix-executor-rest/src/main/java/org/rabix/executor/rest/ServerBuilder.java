package org.rabix.executor.rest;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import org.rabix.executor.ExecutorTransportModuleMQ;
import org.rabix.executor.rest.api.ExecutorHTTPService;
import org.rabix.executor.rest.api.impl.ExecutorHTTPServiceImpl;
import org.rabix.executor.service.ExecutorService;
import org.rabix.executor.transport.TransportQueueConfig;
import org.rabix.executor.transport.impl.TransportStubMQ;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
            new ExecutorTransportModuleMQ(),
            new ExecutorModule(configModule), 
            new AbstractModule() {
              @Override
              protected void configure() {
                bind(ExecutorHTTPService.class).to(ExecutorHTTPServiceImpl.class).in(Scopes.SINGLETON);
                bind(TransportQueueConfig.class).in(Scopes.SINGLETON);
                bind(TransportStubMQ.class).in(Scopes.SINGLETON);
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
    
    ExecutorService executorService = injector.getInstance(ExecutorService.class);
    executorService.startReceiver();
    
    BackendRegister backendRegister = injector.getInstance(BackendRegister.class);
    backendRegister.start();
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
    private TransportStubMQ mqTransportStub;
    private TransportQueueConfig transportQueueConfig;

    private ScheduledExecutorService heartbeatService = Executors.newSingleThreadScheduledExecutor();
    
    @Inject
    public BackendRegister(Configuration configuration, TransportStubMQ mqTransportStub, TransportQueueConfig transportQueueConfig) {
      this.transportQueueConfig = transportQueueConfig;
      this.mqTransportStub = mqTransportStub;
      this.configuration = configuration;
    }
    
    public void start() throws ExecutorException {
      try {
        final BackendMQ backend = registerBackend();

        heartbeatService.scheduleAtFixedRate(new Runnable() {
          @Override
          public void run() {
            mqTransportStub.send(transportQueueConfig.getFromBackendHeartbeatQueue(), new HeartbeatInfo(backend.id, System.currentTimeMillis()));
          }
        }, 0, 10, TimeUnit.SECONDS);
      } catch (Exception e) {
        throw new ExecutorException("Failed to register executor to the Engine", e);
      }
    }

    private BackendMQ registerBackend() {
      String engineHost = configuration.getString("engine.url");
      Integer enginePort = configuration.getInteger("engine.port", null);

      Client client = ClientBuilder.newClient(new ClientConfig().register(LoggingFilter.class));
      WebTarget webTarget = client.target(engineHost + ":" + enginePort + "/v0/engine/backends");

      BackendMQ backend = new BackendMQ(null, transportQueueConfig.getBroker(), transportQueueConfig.getToBackendQueue(), transportQueueConfig.getFromBackendQueue(), transportQueueConfig.getFromBackendHeartbeatQueue());
      Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
      Response response = invocationBuilder.post(Entity.entity(backend, MediaType.APPLICATION_JSON));
      return response.readEntity(BackendMQ.class);
    }
    
    public static class HeartbeatInfo {
      
      @JsonProperty("id")
      private String id;
      @JsonProperty("timestamp")
      private Long timestamp;
      
      @JsonCreator
      public HeartbeatInfo(@JsonProperty("id") String id, @JsonProperty("timestamp") Long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
      }
    }
    
    private static enum BackendType {
      MQ
    }
    
    public static class BackendMQ {
      @JsonProperty("id")
      private final String id;
      @JsonProperty("broker")
      private final String broker;
      @JsonProperty("toBackendQueue")
      private String toBackendQueue;
      @JsonProperty("fromBackendQueue")
      private String fromBackendQueue;
      @JsonProperty("fromBackendHeartbeatQueue")
      private String fromBackendHeartbeatQueue;
      @JsonProperty("type")
      private BackendType type;

      public BackendMQ(@JsonProperty("id") String id, @JsonProperty("broker") String broker, @JsonProperty("toBackendQueue") String toBackendQueue, @JsonProperty("fromBackendQueue") String fromBackendQueue, @JsonProperty("fromBackendHeartbeatQueue") String fromBackendHeartbeatQueue) {
        this.id = id;
        this.type = BackendType.MQ;
        this.broker = broker;
        this.toBackendQueue = toBackendQueue;
        this.fromBackendQueue = fromBackendQueue;
        this.fromBackendHeartbeatQueue = fromBackendHeartbeatQueue;
      }
    }

  }
}
