package org.rabix.engine.rest.plugin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class BackendPluginConfig {

  public static final String BACKEND_PLUGIN_PREFIX = "backend.plugin.";

  private final Configuration configuration;

  @Inject
  public BackendPluginConfig(Configuration configuration) {
    this.configuration = configuration;
  }

  public Set<BackendPluginType> getTypes() {
    Set<BackendPluginType> types = new HashSet<>();

    Iterator<String> propertyKeyIterator = configuration.getKeys();
    while (propertyKeyIterator.hasNext()) {
      String propertyKey = propertyKeyIterator.next();
      if (!propertyKey.startsWith(BACKEND_PLUGIN_PREFIX)) {
        continue;
      }
      BackendPluginType type = getType(propertyKey);
      if (type != null) {
        types.add(type);
      }
    }
    return types;
  }

  public String getString(BackendPluginType type, String key) {
    Preconditions.checkNotNull(type);
    Preconditions.checkNotNull(key);
    return configuration.getString(BACKEND_PLUGIN_PREFIX + type.name().toLowerCase() + "." + key.toLowerCase(), null);
  }

  public Integer getInteger(BackendPluginType type, String key) {
    Preconditions.checkNotNull(type);
    Preconditions.checkNotNull(key);
    return configuration.getInteger(BACKEND_PLUGIN_PREFIX + type.name().toLowerCase() + "." + key.toLowerCase(), null);
  }

  private BackendPluginType getType(String property) {
    property = property.substring(BACKEND_PLUGIN_PREFIX.length());
    String type = property.substring(0, property.indexOf("."));
    if (type == null) {
      return null;
    }

    for (BackendPluginType backendPluginType : BackendPluginType.values()) {
      if (type.equalsIgnoreCase(backendPluginType.name())) {
        return backendPluginType;
      }
    }
    return null;
  }

}
