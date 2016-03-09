package org.rabix.common.json.processor;

/**
 * Bean processor populates and modifies some of the bean properties
 * @param <T>
 */
public interface BeanProcessor<T> {

  /**
   * Process bean 
   */
  T process(T bean) throws BeanProcessorException;
}
