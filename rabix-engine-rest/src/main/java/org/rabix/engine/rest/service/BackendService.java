package org.rabix.engine.rest.service;

import org.rabix.engine.rest.backend.Backend;

public interface BackendService {

  <T extends Backend> T create(T backend);
  
}
