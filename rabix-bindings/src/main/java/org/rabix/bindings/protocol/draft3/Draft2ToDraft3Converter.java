package org.rabix.bindings.protocol.draft3;

public class Draft2ToDraft3Converter {

  public static String convertStepID(String id) {
    if (id.startsWith("#")) {
      return id.substring(1);
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
    return source.replace(".", "/");
  }
}
