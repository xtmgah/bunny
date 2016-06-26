package org.rabix.executor.memoization;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.JSONHelper;
import org.rabix.executor.config.StorageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class LocalMemoizationService {

  private final static Logger logger = LoggerFactory.getLogger(LocalMemoizationService.class);
  
  private Configuration configuration;
  private File memoizationDirectory;
  
  @Inject
  public LocalMemoizationService(Configuration configuration) {
    this.configuration = configuration;
    this.memoizationDirectory = new File(configuration.getString("local.memoization.directory"));
  }
  
  public Map<String, Object> tryToFindResults(Job job) {
    try {
      Bindings bindings = BindingsFactory.create(job);
      File workingDir = new File(memoizationDirectory, StorageConfig.getWorkingDirWithoutBase(job, configuration).getAbsolutePath());
      
      switch (bindings.getProtocolType()) {
      case DRAFT2:
        File resultFile = new File(workingDir, "cwl.output.json");
        if (resultFile.exists()) {
          return JSONHelper.readMap(FileUtils.readFileToString(resultFile));
        }
        break;
      default:
        break;
      }
    } catch (BindingException e) {
      logger.error("Failed to find Bindings", e);
    } catch (IOException e) {
      logger.error("Failed to read result", e);
    }
    return null;
  }
  
}
