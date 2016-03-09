package org.rabix.bindings.protocol.draft2.bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class Draft2Resources {

  @JsonProperty("high_io")
  private final boolean highIO;
  @JsonProperty("cpu")
  private final int cpu;
  @JsonProperty("mem_mb")
  private final int memMB;

  @JsonCreator
  public Draft2Resources(@JsonProperty("high_io") boolean highIO, @JsonProperty("cpu") int cpu,
      @JsonProperty("mem_mb") int memMB) {
    this.highIO = highIO;
    this.cpu = cpu;
    this.memMB = memMB;
  }

  public boolean isHighIO() {
    return highIO;
  }

  public int getCpu() {
    return cpu;
  }

  public int getMemMB() {
    return memMB;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + cpu;
    result = prime * result + (highIO ? 1231 : 1237);
    result = prime * result + memMB;
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
    Draft2Resources other = (Draft2Resources) obj;
    if (cpu != other.cpu)
      return false;
    if (highIO != other.highIO)
      return false;
    if (memMB != other.memMB)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Resources [highIO=" + highIO + ", cpu=" + cpu + ", memMB=" + memMB + "]";
  }

}
