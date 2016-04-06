package org.rabix.engine.rest.plugin;

import org.rabix.engine.rest.plugin.impl.WagnerBackendPlugin;

public enum BackendPluginType {
  WAGNER(WagnerBackendPlugin.class);
  
  private Class<? extends BackendPlugin> clazz;
  
  private BackendPluginType(Class<? extends BackendPlugin> clazz) {
    this.clazz = clazz;
  }
  
  public Class<? extends BackendPlugin> getClazz() {
    return clazz;
  }
}
