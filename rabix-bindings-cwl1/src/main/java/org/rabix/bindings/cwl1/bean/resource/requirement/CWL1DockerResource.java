package org.rabix.bindings.cwl1.bean.resource.requirement;

import org.rabix.bindings.cwl1.bean.resource.CWL1Resource;
import org.rabix.bindings.cwl1.bean.resource.CWL1ResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CWL1DockerResource extends CWL1Resource {

  public static String KEY_DOCKER_PULL = "dockerPull";
  public static String KEY_DOCKER_IMAGE_ID = "dockerImageId";

  @JsonIgnore
  public String getDockerPull() {
    return getValue(KEY_DOCKER_PULL);
  }

  @JsonIgnore
  public String getImageId() {
    return getValue(KEY_DOCKER_IMAGE_ID);
  }

  @Override
  @JsonIgnore
  public CWL1ResourceType getType() {
    return CWL1ResourceType.DOCKER_RESOURCE;
  }
  
  @Override
  public String toString() {
    return "DockerResource [" + raw + "]";
  }
}
