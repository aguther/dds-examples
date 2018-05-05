package com.github.aguther.dds.examples.shape;

import static com.google.common.base.Preconditions.checkNotNull;

import com.rti.dds.infrastructure.ConditionSeq;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.GuardCondition;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.WaitSet;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.ReadCondition;
import com.rti.dds.subscription.ReadConditionParams;
import com.rti.dds.subscription.SampleInfoSeq;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataReaderWatcher<T> implements Closeable, Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataReaderWatcher.class);

  private DataReader dataReader;
  private GuardCondition guardCondition;
  private ReadCondition readCondition;
  private WaitSet waitSet;

  private List<T> sampleSeq;
  private SampleInfoSeq sampleInfoSeq;

  private ScheduledExecutorService executorService;

  private DataReaderWatcherListener<T> listener;

  public DataReaderWatcher(
      DataReader dataReader,
      ReadConditionParams readConditionParams,
      List<T> sampleSeq,
      DataReaderWatcherListener<T> listener
  ) {
    checkNotNull(dataReader);
    checkNotNull(readConditionParams);
    checkNotNull(sampleSeq);
    checkNotNull(listener);

    this.listener = listener;
    this.dataReader = dataReader;

    this.sampleSeq = sampleSeq;
    this.sampleInfoSeq = new SampleInfoSeq();

    guardCondition = new GuardCondition();
    checkNotNull(guardCondition);

    readCondition = dataReader.create_readcondition_w_params(readConditionParams);
    checkNotNull(readCondition);

    // create wait set and attach condition
    waitSet = new WaitSet();
    checkNotNull(waitSet);
    waitSet.attach_condition(readCondition);
    waitSet.attach_condition(guardCondition);

    // create executor and start execution
    executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.scheduleWithFixedDelay(
        this,
        0,
        1,
        TimeUnit.NANOSECONDS
    );
  }

  @Override
  public void close() {
    if (executorService != null) {
      executorService.shutdownNow();
      guardCondition.set_trigger_value(true);
      try {
        executorService.awaitTermination(60, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    if (waitSet != null) {
      if (readCondition != null) {
        waitSet.detach_condition(readCondition);
        dataReader.delete_readcondition(readCondition);
        readCondition = null;
      }
      if (guardCondition != null) {
        waitSet.detach_condition(guardCondition);
        guardCondition.delete();
        guardCondition = null;
      }
      waitSet.delete();
      waitSet = null;
    }
  }

  @Override
  public void run() {
    // wait until condition is triggered
    ConditionSeq conditionSeq = new ConditionSeq();
    waitSet.get_conditions(conditionSeq);
    waitSet.wait(conditionSeq, Duration_t.DURATION_INFINITE);

    try {
      // take data
      dataReader.take_w_condition_untyped(
          sampleSeq,
          sampleInfoSeq,
          ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
          readCondition
      );

      // iterate over data
      for (int i = 0; i < sampleSeq.size(); i++) {
        listener.onDataAvailable(
            sampleSeq.get(i),
            sampleInfoSeq.get(i)
        );
      }

    } finally {
      // return data
      dataReader.return_loan_untyped(
          sampleSeq,
          sampleInfoSeq
      );
    }
  }
}
