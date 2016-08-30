package org.rabix.bindings.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Resources {

  @JsonProperty("cpu")
  private final Long cpu;
  @JsonProperty("memMB")
  private final Long memMB;
  @JsonProperty("diskSpaceMB")
  private final Long diskSpaceMB;
  @JsonProperty("networkAccess")
  private final Boolean networkAccess;
  
  @JsonCreator
  public Resources(@JsonProperty("cpu") Long cpu, @JsonProperty("memMB") Long memMB, @JsonProperty("diskSpaceMB") Long diskSpaceMB, @JsonProperty("networkAccess") Boolean networkAccess) {
    this.cpu = cpu;
    this.memMB = memMB;
    this.diskSpaceMB = diskSpaceMB;
    this.networkAccess = networkAccess;
  }

  public Long getCpu() {
    return cpu;
  }

  public Long getMemMB() {
    return memMB;
  }

  public Long getDiskSpaceMB() {
    return diskSpaceMB;
  }

  public Boolean getNetworkAccess() {
    return networkAccess;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((cpu == null) ? 0 : cpu.hashCode());
    result = prime * result + ((diskSpaceMB == null) ? 0 : diskSpaceMB.hashCode());
    result = prime * result + ((memMB == null) ? 0 : memMB.hashCode());
    result = prime * result + ((networkAccess == null) ? 0 : networkAccess.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Resources other = (Resources) obj;
    if (cpu == null) {
      if (other.cpu != null)
        return false;
    } else if (!cpu.equals(other.cpu))
      return false;
    if (diskSpaceMB == null) {
      if (other.diskSpaceMB != null)
        return false;
    } else if (!diskSpaceMB.equals(other.diskSpaceMB))
      return false;
    if (memMB == null) {
      if (other.memMB != null)
        return false;
    } else if (!memMB.equals(other.memMB))
      return false;
    if (networkAccess == null) {
      if (other.networkAccess != null)
        return false;
    } else if (!networkAccess.equals(other.networkAccess))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Resources [cpu=" + cpu + ", memMB=" + memMB + ", diskSpaceMB=" + diskSpaceMB + ", networkAccess=" + networkAccess + "]";
  }

}
