package org.rabix.executor.pathmapper.local;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.filemapper.FileMappingException;
import org.rabix.executor.config.StorageConfig;

import com.google.inject.Inject;

public class LocalPathMapper implements FileMapper {

  private final Configuration configuration;
  
  @Inject
  public LocalPathMapper(final Configuration configuration) {
    this.configuration = configuration;
  }
  
  @Override
  public String map(String path) throws FileMappingException {
    if (!path.startsWith(File.separator)) {
      try {
        return new File(new File(StorageConfig.getLocalExecutionDirectory(configuration)), path).getCanonicalPath();
      } catch (IOException e) {
        throw new FileMappingException(e);
      }
    }
    return path;
  }

}
