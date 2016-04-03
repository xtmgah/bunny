package org.rabix.bindings;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.rabix.bindings.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingsFactory {

  private final static Logger logger = LoggerFactory.getLogger(BindingsFactory.class);

  private static ConcurrentMap<ProtocolType, Bindings> bindings = new ConcurrentHashMap<>();

  static {
    try {
      for (ProtocolType type : ProtocolType.values()) {
        bindings.put(type, type.getBindingsClass().newInstance());
      }
    } catch (Exception e) {
      logger.error("Failed to initialize bindings", e);
      throw new RuntimeException("Failed to initialize bindings", e);
    }
  }

  public static Bindings create(String appURL) {
    for (Bindings bindings : bindings.values()) {
      try {
        bindings.loadAppObject(appURL);
        return bindings;
      } catch (BindingException e) {
        // do nothing
      }
    }
    return null;
  }

  public static Bindings create(Job job) throws BindingException {
    return create(job.getApp());
  }

  public static Bindings create(ProtocolType type) throws BindingException {
    if (type == null) {
      throw new BindingException("Failed to create bindings. Unsupported protocol.");
    }
    return bindings.get(type);
  }

}
