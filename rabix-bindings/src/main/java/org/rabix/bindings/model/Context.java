package org.rabix.bindings.model;

import java.util.Map;
import java.util.UUID;

public class Context {

  private final String id;
  private final Map<String, String> config;

  public Context(String id, Map<String, String> config) {
    this.id = id;
    this.config = config;
  }

  public static String createUniqueID() {
    return UUID.randomUUID().toString();
  }
  
  public String getId() {
    return id;
  }

  public Map<String, String> getConfig() {
    return config;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((config == null) ? 0 : config.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Context other = (Context) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (config == null) {
      if (other.config != null)
        return false;
    } else if (!config.equals(other.config))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Context [id=" + id + ", config=" + config + "]";
  }

}
