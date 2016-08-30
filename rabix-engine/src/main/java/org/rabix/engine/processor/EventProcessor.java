package org.rabix.engine.processor;

import java.util.List;

import org.rabix.bindings.model.Job;
import org.rabix.engine.event.Event;
import org.rabix.engine.processor.handler.EventHandlerException;

public interface EventProcessor {

  void start(List<IterationCallback> iterationCallbacks, JobStatusCallback jobStatusCallback);

  void stop();
  
  boolean isRunning();

  void send(Event event) throws EventHandlerException;
  
  void addToQueue(Event event);

  /**
   * Post iteration callback 
   */
  public static interface IterationCallback {

    /**
     * Call this method to execute something after one iteration 
     */
    void call(EventProcessor eventProcessor, String contextId, int iteration) throws Exception;
    
  }
  
  /**
   * Job status callback (READY, COMPLETED, etc.)
   */
  public static interface JobStatusCallback {
    
    void onReady(Job job) throws Exception;
    
    void onFailed(Job job) throws Exception;
    
    void onRootCompleted(String string) throws Exception;
    
    void onRootFailed(String string) throws Exception;
    
  }

}
