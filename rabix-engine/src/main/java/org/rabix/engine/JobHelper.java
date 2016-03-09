package org.rabix.engine;

import java.util.UUID;

public class JobHelper {

  public static String generateUniqueId() {
    return UUID.randomUUID().toString();
  }
  
}
