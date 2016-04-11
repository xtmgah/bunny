package org.rabix.bindings.protocol.zero.helper;

public class ZeroHelper {

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
  
  public static String getOutputPath(String filename, String workingDir) {
    return FILE_PREFIX + workingDir + "/" + filename;
  }
  
}
