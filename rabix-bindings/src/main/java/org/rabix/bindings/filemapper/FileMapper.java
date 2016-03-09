package org.rabix.bindings.filemapper;

/**
 * Maps file path from one format to another 
 */
public interface FileMapper {

  /**
   * Map file path
   */
  String map(String path) throws FileMappingException;

}
