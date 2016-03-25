package org.rabix.engine.rest.plugin;

import org.rabix.bindings.model.Job;

public abstract class BackendPlugin {

  protected BackendPluginConfig backendPluginConfig;
  
  public BackendPlugin(BackendPluginConfig backendPluginConfig) {
    this.backendPluginConfig = backendPluginConfig;
  }
  
  public abstract void send(Job job);
  
  public abstract BackendPluginType getType();
}
