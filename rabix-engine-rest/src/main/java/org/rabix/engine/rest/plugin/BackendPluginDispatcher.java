package org.rabix.engine.rest.plugin;

import java.util.List;

import org.rabix.bindings.model.Context;
import org.rabix.bindings.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class BackendPluginDispatcher {

  private final static Logger logger = LoggerFactory.getLogger(BackendPluginDispatcher.class);
  
  public static final String BACKEND_TYPE = "backend.type";

  private final BackendPluginRegister backendPluginRegister;
  
  @Inject
  public BackendPluginDispatcher(BackendPluginRegister backendPluginRegister) {
    this.backendPluginRegister = backendPluginRegister;
  }
  
  public void send(List<Job> jobs) {
    for (Job job : jobs) {
      Context context = job.getContext();

      String backendType = context.getConfig().get(BACKEND_TYPE);
      if (backendType != null) {
        try {
          BackendPluginType backendPluginType = BackendPluginType.valueOf(backendType.toUpperCase());
          BackendPlugin backendPlugin = backendPluginRegister.get(backendPluginType);
          backendPlugin.send(job);
          continue;
        } catch (Exception e) {
          logger.error("Cannot find backend for type " + backendType, e);
        }
      }
      logger.error("There is no backend type. Cannot decide which backend to use");
    }
  }
  
}
