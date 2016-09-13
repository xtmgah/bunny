package org.rabix.bindings.cwl1.expression.javascript;

import org.mozilla.javascript.ClassShutter;

public class CWL1ExpressionDenyAllClassShutter implements ClassShutter {

  @Override
  public boolean visibleToScripts(String arg0) {
    return false;
  }

}
