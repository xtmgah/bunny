package org.rabix.engine.rest.service;

import java.util.List;

import org.rabix.bindings.model.Job;
import org.rabix.engine.processor.EventProcessor;

public interface JobService {

  void update(Job job) throws JobServiceException;
  
  List<Job> getReady(EventProcessor eventProcessor, String contextId) throws JobServiceException;
  
}
