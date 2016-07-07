package org.rabix.bindings.protocol.draft4;

import org.rabix.bindings.protocol.draft3.helper.Draft3SchemaHelper;

public class Draft2ToDraft4Converter {

  public static String convertStepID(String id) {
    if (id.startsWith("#")) {
      return Draft3SchemaHelper.getLastInputId(id.substring(1));
    }
    return id;
  }
  
  public static String convertPortID(String id) {
    if (id.startsWith("#")) {
      return id.substring(1);
    }
    return id;
  }
  
  public static String convertSource(String source) {
    return source.replaceAll("\\.", "/");
  }

  public static String convertDestinationId(String destination) {
    return Draft3SchemaHelper.getLastInputId(destination);
  }
}
