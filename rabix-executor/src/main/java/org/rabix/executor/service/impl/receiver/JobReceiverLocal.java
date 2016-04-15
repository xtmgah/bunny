package org.rabix.executor.service.impl.receiver;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.rabix.bindings.model.Job;
import org.rabix.common.VMQueues;
import org.rabix.common.json.BeanSerializer;
import org.rabix.executor.service.ExecutorService;
import org.rabix.executor.service.JobReceiver;

import com.google.inject.Inject;

public class JobReceiverLocal implements JobReceiver {

  private ExecutorService executorService;

  private ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();

  @Inject
  public JobReceiverLocal(ExecutorService executorService) {
    this.executorService = executorService;
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
    String payload = VMQueues.<String>getQueue(VMQueues.SEND_QUEUE).poll();
    if (payload != null) {
      return BeanSerializer.deserialize(payload, Job.class);
    }
    return null;
  }
  
}
