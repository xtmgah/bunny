package org.rabix.bindings.protocol.draft4.expression.javascript;

import org.mozilla.javascript.ClassShutter;

public class Draft4ExpressionDenyAllClassShutter implements ClassShutter {

  @Override
  public boolean visibleToScripts(String arg0) {
    return false;
  }

}
