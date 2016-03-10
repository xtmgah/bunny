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
   * Returns {@link ProtocolType} 
   */
  ProtocolType getProtocolType();

}
