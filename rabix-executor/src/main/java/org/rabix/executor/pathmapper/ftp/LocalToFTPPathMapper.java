package org.rabix.executor.pathmapper.ftp;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.filemapper.FileMappingException;
import org.rabix.executor.config.StorageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class LocalToFTPPathMapper implements FileMapper {

  private final static Logger logger = LoggerFactory.getLogger(LocalToFTPPathMapper.class);
  
  private Configuration configuration;
  
  @Inject
  public LocalToFTPPathMapper(Configuration configuration) {
    this.configuration = configuration;
  }
  
  @Override
  public String map(String path) throws FileMappingException {
    logger.info("Map absolute physical path {} to relative physical path.", path);
    return path.substring(StorageConfig.getLocalExecutionDirectory(configuration).length() + 1);
  
  }

}
