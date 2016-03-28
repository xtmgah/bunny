package org.rabix.engine.rest.plugin;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

public class BackendPluginRegister {

  private final Map<BackendPluginType, BackendPlugin> backendPlugins = new HashMap<>();
  
  @Inject
  public BackendPluginRegister(BackendPluginConfig backendPluginConfig) {
    register(backendPluginConfig);
  }
  
  private void register(BackendPluginConfig backendPluginConfig) {
    Set<BackendPluginType> types = backendPluginConfig.getTypes();
    
    for (BackendPluginType type : types) {
      Class<? extends BackendPlugin> backendClass = type.getClazz();
      Constructor<? extends BackendPlugin> constructor;
      try {
        constructor = backendClass.getConstructor(BackendPluginConfig.class);
        backendPlugins.put(type, constructor.newInstance(backendPluginConfig));
      } catch (Exception e) {
        throw new RuntimeException("Failed to create BackendPlugin for type " + type);
      }
    }
  }
  
  public BackendPlugin get(BackendPluginType type) {
    return backendPlugins.get(type);
  }
}
