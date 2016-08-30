package org.rabix.engine.processor.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.rabix.engine.event.Event;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.status.EngineStatusCallback;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ShardedEventProcessorImpl implements EventProcessor {

  private int nextEventProcessor = 0;
  private int numberOfCores = Runtime.getRuntime().availableProcessors();
  
  private final ConcurrentMap<String, Integer> rootEventProcessorMapping;
  private final ConcurrentMap<Integer, SingleEventProcessorImpl> eventProcessors;

  private volatile boolean isRunning = false;
  
  @Inject
  public ShardedEventProcessorImpl(Provider<SingleEventProcessorImpl> singleEventProcessorProvider) {
    this.eventProcessors = new ConcurrentHashMap<>(numberOfCores);
    for (int i = 0; i < numberOfCores; i++) {
      this.eventProcessors.put(i, singleEventProcessorProvider.get());
    }
    this.rootEventProcessorMapping = new ConcurrentHashMap<>();
  }

  @Override
  public void start(List<IterationCallback> iterationCallbacks, EngineStatusCallback engineStatusCallback) {
    for (SingleEventProcessorImpl singleEventProcessor : eventProcessors.values()) {
      singleEventProcessor.start(iterationCallbacks, engineStatusCallback);
    }
    this.isRunning = true;
  }

  @Override
  public void stop() {
    for (SingleEventProcessorImpl eventProcessor : eventProcessors.values()) {
      eventProcessor.stop();
    }
    this.isRunning = false;
  }

  @Override
  public void send(Event event) throws EventHandlerException {
    getEventProcessor(event.getContextId()).send(event);
  }

  @Override
  public void addToQueue(Event event) {
    getEventProcessor(event.getContextId()).addToQueue(event);
  }
  
  @Override
  public boolean isRunning() {
    return isRunning;
  }

  private synchronized EventProcessor getEventProcessor(String rootId) {
    Integer eventProcessorID = rootEventProcessorMapping.get(rootId);
    if (eventProcessorID == null) {
      eventProcessorID = (nextEventProcessor++) % numberOfCores;
      rootEventProcessorMapping.put(rootId, eventProcessorID);
    }
    return eventProcessors.get(eventProcessorID);
  }
}
