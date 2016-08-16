package org.rabix.executor.pathmapper.ftp;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.filemapper.FileMappingException;
import org.rabix.executor.config.StorageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class FTPToLocalPathMapper implements FileMapper {

  private final static Logger logger = LoggerFactory.getLogger(FTPToLocalPathMapper.class);
  
  private final Configuration configuration;
  
  @Inject
  public FTPToLocalPathMapper(Configuration configuration) {
    this.configuration = configuration;
  }
  
  @Override
  public String map(String path, Map<String, Object> config) throws FileMappingException {
    logger.info("Map FTP path {} to physical path.", path);
    try {
      return new File(new File(StorageConfig.getLocalExecutionDirectory(configuration)), path).getCanonicalPath();
    } catch (IOException e) {
      throw new FileMappingException(e);
    }
  }

}
