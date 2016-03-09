package org.rabix.common.json;

/**
 * Bean property views used in Jackson processing
 */
public interface BeanPropertyView {

  /**
   * Property view that enables partial visibility of a bean
   */
  public static class Partial implements BeanPropertyView {};

  /**
   * Property view that enables full visibility of a bean
   */
  public static class Full implements BeanPropertyView {};
}
