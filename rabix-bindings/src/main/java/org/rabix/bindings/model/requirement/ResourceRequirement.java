package org.rabix.bindings.model.requirement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceRequirement implements Requirement {

  @JsonProperty("cpuMin")
  private final Integer cpuMin;
  @JsonProperty("cpuRecommended")
  private final Integer cpuRecommended;
  @JsonProperty("memMinMB")
  private final Integer memMinMB;
  @JsonProperty("memRecommendedMB")
  private final Integer memRecommendedMB;
  @JsonProperty("diskSpaceMinMB")
  private final Integer diskSpaceMinMB;
  @JsonProperty("diskSpaceRecommendedMB")
  private final Integer diskSpaceRecommendedMB;
  @JsonProperty("networkAccess")
  private final Boolean networkAccess;

  @JsonCreator
  public ResourceRequirement(@JsonProperty("cpuMin") Integer cpuMin,
      @JsonProperty("cpuRecommended") Integer cpuRecommended, @JsonProperty("memMinMB") Integer memMinMB,
      @JsonProperty("memRecommendedMB") Integer memRecommendedMB,
      @JsonProperty("diskSpaceMinMB") Integer diskSpaceMinMB,
      @JsonProperty("diskSpaceRecommendedMB") Integer diskSpaceRecommendedMB,
      @JsonProperty("networkAccess") Boolean networkAccess) {
    this.cpuMin = cpuMin;
    this.cpuRecommended = cpuRecommended;
    this.memMinMB = memMinMB;
    this.memRecommendedMB = memRecommendedMB;
    this.diskSpaceMinMB = diskSpaceMinMB;
    this.diskSpaceRecommendedMB = diskSpaceRecommendedMB;
    this.networkAccess = networkAccess;
  }

  public Integer getCpuMin() {
    return cpuMin;
  }

  public Integer getCpuRecommended() {
    return cpuRecommended;
  }

  public Integer getMemMinMB() {
    return memMinMB;
  }

  public Integer getMemRecommendedMB() {
    return memRecommendedMB;
  }

  public Integer getDiskSpaceMinMB() {
    return diskSpaceMinMB;
  }

  public Integer getDiskSpaceRecommendedMB() {
    return diskSpaceRecommendedMB;
  }

  public Boolean getNetworkAccess() {
    return networkAccess;
  }

  @Override
  public String toString() {
    return "ResourceRequirement [cpuMin=" + cpuMin + ", cpuRecommended=" + cpuRecommended + ", memMinMB=" + memMinMB
        + ", memRecommendedMB=" + memRecommendedMB + ", diskSpaceMinMB=" + diskSpaceMinMB + ", diskSpaceRecommendedMB="
        + diskSpaceRecommendedMB + ", networkAccess=" + networkAccess + "]";
  }

}
