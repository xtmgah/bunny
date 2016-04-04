package org.rabix.bindings;

import java.io.File;

import org.rabix.bindings.model.Executable;

public interface Bindings extends CommandLineBuilder, ProtocolValueOperator, ProtocolProcessor, ProtocolTranslator, RequirementProvider, ResultCollector {

  /**
   * Loads application from the file 
   */
  String loadAppFromFile(File file) throws BindingException;
  
  /**
   * Gets native application 
   */
  Object getApp(Executable executable) throws BindingException;
  
  /**
   * Is container required or it can be executed by itself (expression tools, ...)
   */
  boolean isSelfExecutable(Executable executable) throws BindingException;

  /**
   * Returns {@link ProtocolType} 
   */
  ProtocolType getProtocolType();

}
