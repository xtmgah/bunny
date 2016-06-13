package org.rabix.engine.processor.handler;

import org.rabix.engine.event.Event;

import com.google.inject.persist.Transactional;

/**
 * Describes an event handler interface
 */
public interface EventHandler<T extends Event> {

  /**
   * Handles the event
   */
  @Transactional
  void handle(T event) throws EventHandlerException;

}
