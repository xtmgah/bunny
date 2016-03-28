package org.rabix.bindings;

public interface DocumentReferenceResolver {

  String resolve(String uri) throws BindingException;
  
}
