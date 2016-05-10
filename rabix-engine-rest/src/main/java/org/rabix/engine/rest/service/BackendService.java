package org.rabix.engine.rest.service;

import org.rabix.transport.backend.Backend;

public interface BackendService {

  <T extends Backend> T create(T backend);
  
}
