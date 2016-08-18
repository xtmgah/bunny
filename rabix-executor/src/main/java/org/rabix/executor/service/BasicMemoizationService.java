package org.rabix.executor.service;

import java.util.Map;

import org.rabix.bindings.model.Job;

public interface BasicMemoizationService {

  Map<String, Object> tryToFindResults(Job job);
  
}
