package org.rabix.bindings.filemapper;

import java.util.Map;

/**
 * Maps file path from one format to another 
 */
public interface FileMapper {

  /**
   * Map file path
   */
  String map(String path, Map<String, Object> config) throws FileMappingException;

}
