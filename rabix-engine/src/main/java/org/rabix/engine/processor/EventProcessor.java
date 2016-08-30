package org.rabix.engine.processor;

import java.util.List;

import org.rabix.engine.event.Event;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.status.EngineStatusCallback;

public interface EventProcessor {

  void start(List<IterationCallback> iterationCallbacks, EngineStatusCallback engineStatusCallback);

  void stop();
  
  boolean isRunning();

  void send(Event event) throws EventHandlerException;
  
  void addToQueue(Event event);

  /**
   * Post iteration callback 
   */
  interface IterationCallback {

    /**
     * Call this method to execute something after one iteration 
     */
    void call(EventProcessor eventProcessor, String contextId, int iteration) throws Exception;
    
  }
  
}
