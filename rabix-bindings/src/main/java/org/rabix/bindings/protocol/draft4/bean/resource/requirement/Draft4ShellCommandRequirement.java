package org.rabix.bindings.protocol.draft4.bean.resource.requirement;

import org.rabix.bindings.protocol.draft4.bean.resource.Draft4Resource;
import org.rabix.bindings.protocol.draft4.bean.resource.Draft4ResourceType;

public class Draft4ShellCommandRequirement extends Draft4Resource {

  @Override
  public Draft4ResourceType getType() {
    return Draft4ResourceType.SHELL_COMMAND_REQUIREMENT;
  }
  
}
