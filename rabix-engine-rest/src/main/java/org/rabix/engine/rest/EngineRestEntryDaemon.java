package org.rabix.engine.rest;

import java.io.File;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineRestEntryDaemon implements Daemon {

  private final static Logger logger = LoggerFactory.getLogger(EngineRestEntryDaemon.class);
  
  private String configurationDir;
  
  @Override
  public void destroy() {
    logger.info("Bunny engine daemon destroyed.");
  }

  @Override
  public void init(DaemonContext context) throws DaemonInitException, Exception {
    logger.info("Bunny engine daemon initializing...");
    
    configurationDir = context.getArguments()[0];
  }

  @Override
  public void start() throws Exception {
    logger.info("Bunny engine starting");
    
    File configDir = new File(configurationDir);
    Server server = new ServerBuilder(configDir).build();
    try {
      server.start();
      server.join();
    }
    finally {
      server.destroy();
    }
  }

  @Override
  public void stop() throws Exception {
    logger.info("Bunny engine stopped");
  }

}
