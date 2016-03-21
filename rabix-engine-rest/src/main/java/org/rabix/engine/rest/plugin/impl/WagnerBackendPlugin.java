package org.rabix.engine.rest.plugin.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.rabix.bindings.model.Executable;
import org.rabix.engine.rest.plugin.BackendPlugin;
import org.rabix.engine.rest.plugin.BackendPluginConfig;
import org.rabix.engine.rest.plugin.BackendPluginType;

public class WagnerBackendPlugin extends BackendPlugin {

  private final String uri;
  
  private Set<Executable> runningExecutables = new HashSet<>();
  
  private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()/2);
  
  public WagnerBackendPlugin(BackendPluginConfig backendPluginConfig) {
    super(backendPluginConfig);
    this.uri = getHost() + ":" + getPort();
  }
  
  @Override
  public synchronized void send(final Executable executable) {
    if (!runningExecutables.contains(executable)) {
      runningExecutables.add(executable);
      
      executorService.submit(new Runnable() {
        @Override
        @SuppressWarnings("unchecked")
        public void run() {
          Client client = ClientBuilder.newClient(new ClientConfig().register(LoggingFilter.class));
          WebTarget webTarget = client.target(path("/v1/jobs"));
          Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON).header("context-id", executable.getContext().getId());
          Response response = invocationBuilder.post(Entity.entity(executable, MediaType.APPLICATION_JSON));
          Map<String, String> responseMap = response.readEntity(Map.class);
          System.out.println(responseMap); 
        }
      });
    }
  }
  
  private String path(String path) {
    path = path.startsWith("/") ? path.substring(1) : path;
    return uri.endsWith("/") ? uri + path : uri + "/" + path;
  }
  
  private String getHost() {
    return backendPluginConfig.getString(getType(), "host");
  }
  
  private Integer getPort() {
    return backendPluginConfig.getInteger(getType(), "port");
  }

  @Override
  public BackendPluginType getType() {
    return BackendPluginType.WAGNER;
  }
  
}
