package org.rabix.bindings.protocol.draft2.expression.javascript;

import org.mozilla.javascript.ClassShutter;

public class Draft2ExpressionDenyAllClassShutter implements ClassShutter {

  @Override
  public boolean visibleToScripts(String arg0) {
    return false;
  }

}
