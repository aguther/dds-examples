package com.github.aguther.dds.util;

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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataReaderWatcher<T> implements Closeable, Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataReaderWatcher.class);

  private DataReader dataReader;
  private GuardCondition guardCondition;
  private ReadCondition readCondition;
  private WaitSet waitSet;

  private ExecutorService executorService;

  private DataReaderWatcherListener<T> listener;
  private DataReaderWatcherExecutor<T> executor;

  public DataReaderWatcher(
      DataReader dataReader,
      ReadConditionParams readConditionParams,
      DataReaderWatcherExecutor<T> executor,
      DataReaderWatcherListener<T> listener
  ) {
    checkNotNull(dataReader);
    checkNotNull(readConditionParams);
    checkNotNull(executor);
    checkNotNull(listener);

    this.dataReader = dataReader;
    this.executor = executor;
    this.listener = listener;

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
    executorService = Executors.newSingleThreadExecutor();
    executorService.submit(this);
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
    do {
      // wait until condition is triggered
      ConditionSeq conditionSeq = new ConditionSeq();
      waitSet.get_conditions(conditionSeq);
      waitSet.wait(conditionSeq, Duration_t.DURATION_INFINITE);

      // check if we shutdown was triggered -> early exit
      if (guardCondition.get_trigger_value()) {
        return;
      }

      // read or take samples
      executor.execute(
          dataReader,
          readCondition,
          listener
      );

    } while (!guardCondition.get_trigger_value());
  }
}
