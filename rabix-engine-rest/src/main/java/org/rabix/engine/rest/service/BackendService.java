package org.rabix.engine.rest.service;

import org.rabix.transport.backend.Backend;
import org.rabix.transport.mechanism.TransportPluginException;

public interface BackendService {

  <T extends Backend> T create(T backend) throws TransportPluginException;
  
}
