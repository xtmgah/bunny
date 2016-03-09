package org.rabix.common.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloneHelper {

  private final static Logger logger = LoggerFactory.getLogger(CloneHelper.class);

  private CloneHelper() {
  }

  public static Object deepCopy(Object oldObj) throws Exception {
    ObjectOutputStream oos = null;
    ObjectInputStream ois = null;
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      oos = new ObjectOutputStream(bos);
      oos.writeObject(oldObj);
      oos.flush();
      ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
      ois = new ObjectInputStream(bin);
      return ois.readObject();
    } catch (Exception e) {
      logger.error("Failed to clone " + oldObj, e);
      throw (e);
    } finally {
      if (oos != null) {
        oos.close();
      }
      if (ois != null) {
        ois.close();
      }
    }
  }

}
