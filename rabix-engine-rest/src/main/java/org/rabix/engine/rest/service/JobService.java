package org.rabix.engine.rest.service;

import java.util.List;
import java.util.Set;

import org.rabix.bindings.model.Job;
import org.rabix.engine.processor.EventProcessor;

public interface JobService {

  void update(Job job) throws JobServiceException;
  
  Set<Job> getReady(EventProcessor eventProcessor, String contextId) throws JobServiceException;

  Job create(Job job) throws JobServiceException;
  
  List<Job> get() throws EngineRestServiceException;
  
  Job get(String id) throws EngineRestServiceException;
}
