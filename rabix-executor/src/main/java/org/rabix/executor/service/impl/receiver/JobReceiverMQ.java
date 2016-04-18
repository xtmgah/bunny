package org.rabix.executor.service.impl.receiver;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.rabix.bindings.model.Job;
import org.rabix.executor.service.ExecutorService;
import org.rabix.executor.service.JobReceiver;
import org.rabix.executor.transport.TransportQueueConfig;
import org.rabix.executor.transport.TransportStub.ResultPair;
import org.rabix.executor.transport.impl.TransportStubMQ;

import com.google.inject.Inject;

public class JobReceiverMQ implements JobReceiver {

  private final TransportStubMQ mqTransportStub;
  private final ExecutorService executorService;
  private final TransportQueueConfig transportQueueConfig;

  private ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();

  @Inject
  public JobReceiverMQ(ExecutorService executorService, TransportQueueConfig transportQueueConfig, TransportStubMQ mqTransportStub) {
    this.mqTransportStub = mqTransportStub;
    this.executorService = executorService;
    this.transportQueueConfig = transportQueueConfig;
  }

  @Override
  public void stop() {
    scheduledService.shutdown();
  }

  public void start() {
    scheduledService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        Job job = receive();
        if (job != null) {
          executorService.start(job, job.getContext().getId());
        }
      }
    }, 0, 1, TimeUnit.SECONDS);
  }

  @Override
  public Job receive() {
    ResultPair<Job> result = mqTransportStub.receive(transportQueueConfig.getToBackendQueue(), Job.class);
    if (result.isSuccess() && result.getResult() != null) {
      return result.getResult();
    }
    return null;
  }

}
