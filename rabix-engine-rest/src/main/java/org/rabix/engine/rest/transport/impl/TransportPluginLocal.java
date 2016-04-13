package org.rabix.engine.rest.transport.impl;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;

public class TransportPluginLocal {

  public static interface LocalExecutor {

    void stop(Job job);

    void start(Job job);

    JobStatus findStatus(String id);

  }

}
