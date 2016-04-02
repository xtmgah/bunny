package org.rabix.bindings;

public interface ProtocolDocumentResolver {

  String resolve(String uri) throws BindingException;
  
}
