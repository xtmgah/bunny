package org.rabix.bindings.sb.expression.javascript;

import org.mozilla.javascript.ClassShutter;

public class SBExpressionDenyAllClassShutter implements ClassShutter {

  @Override
  public boolean visibleToScripts(String arg0) {
    return false;
  }

}
