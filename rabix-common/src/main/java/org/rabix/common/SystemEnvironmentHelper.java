package org.rabix.common;

import java.lang.management.ManagementFactory;

public class SystemEnvironmentHelper {

  private static final long MEGABYTE = 1024L * 1024L;
  
  @SuppressWarnings("restriction")
  public static long getTotalPhysicalMemorySizeInMB() {
    com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    long totalPhysicalMemorySize = bytesToMeg(operatingSystemMXBean.getTotalPhysicalMemorySize());
    long jvmMaxMemory = bytesToMeg(Runtime.getRuntime().maxMemory());
    return totalPhysicalMemorySize - jvmMaxMemory;
  }
  
  @SuppressWarnings("restriction")
  public static long getNumberOfCores() {
    com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    return operatingSystemMXBean.getAvailableProcessors();
  }
  
  public static long bytesToMeg(long bytes) {
    return bytes / MEGABYTE ;
   }
}
