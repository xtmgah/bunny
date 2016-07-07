package org.rabix.bindings.protocol.draft4.bean.resource.requirement;

import org.rabix.bindings.protocol.draft4.bean.resource.Draft4Resource;
import org.rabix.bindings.protocol.draft4.bean.resource.Draft4ResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Draft4DockerResource extends Draft4Resource {

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
  public Draft4ResourceType getType() {
    return Draft4ResourceType.DOCKER_RESOURCE;
  }
  
  @Override
  public String toString() {
    return "DockerResource [" + raw + "]";
  }
}
