package org.rabix.bindings.protocol.rabix.helper;

public class RabixHelper {

  public final static String FILE_PREFIX = "file://";
  
  public static boolean isFile(Object value) {
    if (value == null) {
      return false;
    }
    return value instanceof String && ((String) value).startsWith(FILE_PREFIX);
  }
  
  public static String getFilePath(Object value) {
    if (!isFile(value)) {
      return null;
    }
    return ((String) value).substring(FILE_PREFIX.length());
  }
  
}
