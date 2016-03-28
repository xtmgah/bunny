package org.rabix.bindings;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.draft2.bean.Draft2JobApp;
import org.rabix.common.json.BeanSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingsFactory {
  
  private final static Logger logger = LoggerFactory.getLogger(BindingsFactory.class);

  private static ConcurrentMap<ProtocolType, Bindings> bindings = new ConcurrentHashMap<>();
  
  static {
    try {
      for (ProtocolType type : ProtocolType.values()) {
        bindings.put(type, new BindingsImpl(type));
      }
    } catch (BindingException e) {
      logger.error("Failed to initialize bindings", e);
      throw new RuntimeException("Failed to initialize bindings", e);
    }
  }
  
  public static BindingsPair create(String appURL) {
    for (Bindings binding : bindings.values()) {
      try {
        String resolvedApp = binding.loadApp(appURL);
        return new BindingsPair(binding, resolvedApp);  
      } catch (BindingException e) {
        // do nothing
      }
    }
    return null;
  }
  
  public static Bindings createFromAppText(String appStr) throws BindingException {
    return create(sniffProtocolFromAppText(appStr));
  }
  
  public static Bindings create(Job job) throws BindingException {
    return create(sniffProtocol(job));
  }
  
  public static Bindings create(ProtocolType type) throws BindingException {
    if (type == null) {
      throw new BindingException("Failed to create bindings. Unsupported protocol.");
    }
    return bindings.get(type);
  }
 
  private static ProtocolType sniffProtocol(Job job) {
    try {
      job.getApp(Draft2JobApp.class);
      return ProtocolType.DRAFT2;
    } catch (Exception e) {
      // do nothing
    }
    return null;
  }
  
  private static ProtocolType sniffProtocolFromAppText(String appStr) {
    try {
      BeanSerializer.deserialize(appStr, Draft2JobApp.class);
      return ProtocolType.DRAFT2;
    } catch (Exception e) {
      // do nothing
    }
    return null;
  }
  
  public static class BindingsPair {
    private final String resolved;
    private final Bindings bindings;
    
    public BindingsPair(Bindings bindings, String resolved) {
      this.bindings = bindings;
      this.resolved = resolved;
    }

    public String getResolved() {
      return resolved;
    }

    public Bindings getBindings() {
      return bindings;
    }
    
  }
  
}
