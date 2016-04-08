package org.rabix.engine.rest.api;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rabix.engine.rest.model.Backend;

@Produces(MediaType.APPLICATION_JSON)
@Path("/v0/engine/backends")
public interface BackendHTTPService {

  @POST
  Response create(Backend job);

}
