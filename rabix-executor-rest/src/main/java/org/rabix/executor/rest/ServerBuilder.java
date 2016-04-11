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
import org.rabix.executor.mq.MQConfig;
import org.rabix.executor.mq.MQTransportStub;
import org.rabix.executor.rest.api.ExecutorHTTPService;
import org.rabix.executor.rest.api.impl.ExecutorHTTPServiceImpl;
import org.rabix.executor.service.ExecutorService;

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

  public Server build() {
    ServiceLocator locator = BootstrapUtils.newServiceLocator();

    ConfigModule configModule = new ConfigModule(configDir, null);
    Injector injector = BootstrapUtils.newInjector(locator,
        Arrays.asList(new ServletModule(), new ExecutorModule(configModule), new AbstractModule() {
          @Override
          protected void configure() {
            bind(ExecutorHTTPService.class).to(ExecutorHTTPServiceImpl.class).in(Scopes.SINGLETON);
            bind(MQConfig.class).in(Scopes.SINGLETON);
            bind(MQTransportStub.class).in(Scopes.SINGLETON);
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

    private MQConfig mqConfig;
    private Configuration configuration;
    private MQTransportStub mqTransportStub;

    private ScheduledExecutorService heartbeatService = Executors.newSingleThreadScheduledExecutor();
    
    @Inject
    public BackendRegister(Configuration configuration, MQTransportStub mqTransportStub, MQConfig mqConfig) {
      this.mqConfig = mqConfig;
      this.mqTransportStub = mqTransportStub;
      this.configuration = configuration;
    }
    
    public void start() {
      final Backend backend = registerBackend();
      
      heartbeatService.scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
          mqTransportStub.send(mqConfig.getHeartbeatQueue(), new HeartbeatInfo(backend.id, System.currentTimeMillis()));
        }
      }, 0, 10, TimeUnit.SECONDS);
    }

    private Backend registerBackend() {
      String engineHost = configuration.getString("engine.url");
      Integer enginePort = configuration.getInteger("engine.port", null);

      Client client = ClientBuilder.newClient(new ClientConfig().register(LoggingFilter.class));
      WebTarget webTarget = client.target(engineHost + ":" + enginePort + "/v0/engine/backends");

      Backend backend = new Backend(null, mqConfig.getBroker(), mqConfig.getSendQueue(), mqConfig.getReceiveQueue(), mqConfig.getHeartbeatQueue());
      Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
      Response response = invocationBuilder.post(Entity.entity(backend, MediaType.APPLICATION_JSON));
      return response.readEntity(Backend.class);
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
    
    public static class Backend {
      
      @JsonProperty("id")
      private final String id;
      @JsonProperty("broker")
      private final String broker;
      @JsonProperty("sendQueue")
      private final String sendQueue;
      @JsonProperty("receiveQueue")
      private final String receiveQueue;
      @JsonProperty("heartbeatQueue")
      private final String heartbeatQueue;

      public Backend(@JsonProperty("id") String id, @JsonProperty("broker") String broker, @JsonProperty("sendQueue") String sendQueue, @JsonProperty("receiveQueue") String receiveQueue, @JsonProperty("heartbeatQueue") String heartbeatQueue) {
        this.id = id;
        this.broker = broker;
        this.sendQueue = sendQueue;
        this.receiveQueue = receiveQueue;
        this.heartbeatQueue = heartbeatQueue;
      }

    }

  }
}
