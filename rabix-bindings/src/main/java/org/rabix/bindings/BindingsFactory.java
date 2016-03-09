package org.rabix.bindings;

import org.rabix.bindings.model.Executable;
import org.rabix.bindings.protocol.draft2.bean.Draft2JobApp;
import org.rabix.common.json.BeanSerializer;

public class BindingsFactory {

  public static Bindings create(Object app) throws BindingException {
    return create(sniffProtocol(app));
  }
  
  public static Bindings createFromAppText(String appStr) throws BindingException {
    return create(sniffProtocolFromAppText(appStr));
  }
  
  public static Bindings create(Executable executable) throws BindingException {
    return create(sniffProtocol(executable));
  }
  
  public static Bindings create(ProtocolType type) throws BindingException {
    if (type == null) {
      throw new BindingException("Failed to create bindings. Unsupported protocol.");
    }
    return new BindingsImpl(type);
  }
 
  public static ProtocolType sniffProtocol(Executable executable) {
    try {
      executable.getApp(Draft2JobApp.class);
      return ProtocolType.DRAFT2;
    } catch (Exception e) {
      // do nothing
    }
    return null;
  }
  
  public static ProtocolType sniffProtocol(Object app) {
    try {
      if (app instanceof Draft2JobApp) {
        return ProtocolType.DRAFT2;
      }
    } catch (Exception e) {
      // do nothing
    }
    return null;
  }
  
  public static ProtocolType sniffProtocolFromAppText(String appStr) {
    try {
      BeanSerializer.deserialize(appStr, Draft2JobApp.class);
      return ProtocolType.DRAFT2;
    } catch (Exception e) {
      // do nothing
    }
    return null;
  }
  
}
