package org.rabix.common.helper;

import java.io.File;
import java.io.IOException;

public class FileHelper {

  public static void serialize(File file, Object object) {
    if (object == null) {
      return;
    }
    try {
      if (!file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }
      if (!file.exists()) {
        file.createNewFile();
      }
      JSONHelper.mapper.writerWithDefaultPrettyPrinter().writeValue(file, object);
    } catch (IOException e) {
      throw new RuntimeException("Failed to serialize to " + file);
    }
  }
  
}
