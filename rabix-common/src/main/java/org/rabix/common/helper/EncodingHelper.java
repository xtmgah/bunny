package org.rabix.common.helper;

import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;
import org.rabix.common.json.BeanSerializer;

public class EncodingHelper {

  public static Charset DEFAULT_ENCODING = Charset.forName("UTF-8");
  
  public static String encodeBase64(Object data) {
    String json = BeanSerializer.serializeFull(data);
    return Base64.encodeBase64String(json.getBytes(DEFAULT_ENCODING));
  }

  public static <T> T decodeBase64(String data, Class<T> clazz) {
    byte[] dataBytes = Base64.decodeBase64(data);
    String dataStr = new String(dataBytes, DEFAULT_ENCODING);
    return BeanSerializer.deserialize(dataStr, clazz);
  }
  
}
