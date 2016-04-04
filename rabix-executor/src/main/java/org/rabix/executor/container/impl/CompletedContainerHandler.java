package org.rabix.executor.container.impl;

import java.io.File;

import org.rabix.executor.container.ContainerException;
import org.rabix.executor.container.ContainerHandler;

public class CompletedContainerHandler implements ContainerHandler {

  @Override
  public void start() throws ContainerException {
    // do nothing
  }

  @Override
  public void stop() throws ContainerException {
    // do nothing
  }

  @Override
  public boolean isStarted() throws ContainerException {
    return true;
  }

  @Override
  public boolean isRunning() throws ContainerException {
    return false;
  }

  @Override
  public int getProcessExitStatus() throws ContainerException {
    return 0;
  }

  @Override
  public void dumpContainerLogs(File errorFile) throws ContainerException {
    // do nothing
  }

}
