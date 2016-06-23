package org.rabix.executor.service.impl;

import java.lang.management.ManagementFactory;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.ResourceRequirement;
import org.rabix.executor.service.JobFitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class JobFitterImpl implements JobFitter {

  private static final long MEGABYTE = 1024L * 1024L;

  private static final Logger logger = LoggerFactory.getLogger(JobFitterImpl.class);
  
  private Long availableCores;
  private Long availableMemory;

  private boolean isEnabled;

  @SuppressWarnings("restriction")
  @Inject
  public JobFitterImpl(Configuration configuration) {
    com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    long totalPhysicalMemorySize = bytesToMeg(operatingSystemMXBean.getTotalPhysicalMemorySize());
    long jvmMaxMemory = bytesToMeg(Runtime.getRuntime().maxMemory());
    
    this.availableMemory = totalPhysicalMemorySize - jvmMaxMemory;
    this.availableCores = (long) operatingSystemMXBean.getAvailableProcessors();
    this.isEnabled = configuration.getBoolean("resource.fitter.enabled", false);
  }
  
  public static long bytesToMeg(long bytes) {
    return bytes / MEGABYTE ;
   }
  
  @Override
  public synchronized boolean tryToFit(Job job) throws BindingException {
    if (!isEnabled) {
      return true;
    }
    Bindings bindings = BindingsFactory.create(job);
    if (bindings.canExecute(job)) {
      return true;
    }
    ResourceRequirement resourceRequirement = bindings.getResourceRequirement(job);

    Long cpu = resourceRequirement.getCpuMin();
    if (cpu != null && cpu > availableCores) {
      return false;
    }

    Long memory = resourceRequirement.getMemMinMB();
    if (memory != null && memory > availableMemory) {
      return false;
    }
    availableCores -= cpu != null ? cpu : 0;
    availableMemory -= memory != null ? memory : 0;
    logger.info("Job {} fits. Starting execution...", job.getId());
    return true;
  }

  @Override
  public synchronized void free(Job job) throws BindingException {
    if (!isEnabled) {
      return;
    }
    
    Bindings bindings = BindingsFactory.create(job);
    if (bindings.canExecute(job)) {
      return;
    }
    
    ResourceRequirement resourceRequirement = bindings.getResourceRequirement(job);

    availableCores += resourceRequirement.getCpuMin() != null ? resourceRequirement.getCpuMin() : 0;
    availableMemory += resourceRequirement.getMemMinMB() != null ? resourceRequirement.getMemMinMB() : 0;
    
    logger.info("Job {} freed reqsources.", job.getId());
  }
  
}
