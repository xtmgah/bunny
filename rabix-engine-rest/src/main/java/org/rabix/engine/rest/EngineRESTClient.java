package org.rabix.engine.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.rabix.engine.rest.dto.Task;

public class EngineRESTClient {

  private final int port;
  private final String host;

  public EngineRESTClient(String host, int port) {
    this.host = host;
    this.port = port;
  }
  
  public void upload(Task task) {
    Client client = ClientBuilder.newClient(new ClientConfig().register(LoggingFilter.class));
    WebTarget webTarget = client.target(host + ":" + port + "/v0/engine");

    Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
    invocationBuilder.post(Entity.entity(task, MediaType.APPLICATION_JSON));
  }
  
}
