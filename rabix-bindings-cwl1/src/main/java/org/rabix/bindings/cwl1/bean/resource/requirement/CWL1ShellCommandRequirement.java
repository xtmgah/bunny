package org.rabix.bindings.cwl1.bean.resource.requirement;

import org.rabix.bindings.cwl1.bean.resource.CWL1Resource;
import org.rabix.bindings.cwl1.bean.resource.CWL1ResourceType;

public class CWL1ShellCommandRequirement extends CWL1Resource {

  @Override
  public CWL1ResourceType getType() {
    return CWL1ResourceType.SHELL_COMMAND_REQUIREMENT;
  }
  
}
