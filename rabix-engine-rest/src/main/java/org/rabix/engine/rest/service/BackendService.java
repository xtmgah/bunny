package org.rabix.engine.rest.service;

import java.util.List;

import org.rabix.engine.rest.db.BackendRecord;
import org.rabix.transport.backend.Backend;

public interface BackendService {

  List<BackendRecord> findActive() throws EngineRestServiceException;
  
  <T extends Backend> T create(T backend) throws EngineRestServiceException;
  
  void updateHeartbeat(String id, Long heartbeat) throws EngineRestServiceException;

  void update(BackendRecord backendRecord) throws EngineRestServiceException;
  
}
