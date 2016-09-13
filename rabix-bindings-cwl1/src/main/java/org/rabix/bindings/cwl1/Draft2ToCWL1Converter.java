package org.rabix.bindings.cwl1;

import org.rabix.bindings.cwl1.helper.CWL1SchemaHelper;

public class Draft2ToCWL1Converter {

  public static String convertStepID(String id) {
    if (id.startsWith("#")) {
      return CWL1SchemaHelper.getLastInputId(id.substring(1));
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
    return CWL1SchemaHelper.getLastInputId(destination);
  }
}
