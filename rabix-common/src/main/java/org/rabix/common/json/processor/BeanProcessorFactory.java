package org.rabix.common.json.processor;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class BeanProcessorFactory {

  private static final Logger logger = LoggerFactory.getLogger(BeanProcessorFactory.class);

  @SuppressWarnings("unchecked")
  public static <T> BeanProcessor<T> create(Class<T> clazz) {
    Preconditions.checkNotNull(clazz);

    BeanProcessorClass beanProcessorAnnotation = clazz.getAnnotation(BeanProcessorClass.class);
    if (beanProcessorAnnotation == null) {
      return null;
    }

    Class<? extends BeanProcessor<?>> processorClass = beanProcessorAnnotation.name();
    if (processorClass == null) {
      return null;
    }
    try {
      Constructor<? extends BeanProcessor<?>> constructor = processorClass.getDeclaredConstructor();
      constructor.setAccessible(true);
      return (BeanProcessor<T>) constructor.newInstance();
    } catch (Exception e) {
      logger.error("Couldn't create processor for " + clazz.getCanonicalName(), e);
      throw new RuntimeException(e);
    }
  }

}
