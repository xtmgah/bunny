package org.rabix.bindings;

import java.io.File;

public interface DocumentReferenceResolver {

  String resolve(File file) throws BindingException;
  
}
