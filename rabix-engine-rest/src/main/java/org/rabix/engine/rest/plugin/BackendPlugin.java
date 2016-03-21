package org.rabix.engine.rest.plugin;

import org.rabix.bindings.model.Executable;

public abstract class BackendPlugin {

  protected BackendPluginConfig backendPluginConfig;
  
  public BackendPlugin(BackendPluginConfig backendPluginConfig) {
    this.backendPluginConfig = backendPluginConfig;
  }
  
  public abstract void send(Executable executable);
  
  public abstract BackendPluginType getType();
}
