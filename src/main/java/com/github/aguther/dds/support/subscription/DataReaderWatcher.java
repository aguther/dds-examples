/*
 * MIT License
 *
 * Copyright (c) 2019 Andreas Guther
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.aguther.dds.support.subscription;

import static com.google.common.base.Preconditions.checkNotNull;

import com.rti.dds.infrastructure.ConditionSeq;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.GuardCondition;
import com.rti.dds.infrastructure.WaitSet;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.ReadCondition;
import com.rti.dds.subscription.ReadConditionParams;
import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataReaderWatcher<T> implements Closeable, Runnable {

  private static final Logger LOGGER = LogManager.getLogger(DataReaderWatcher.class);

  private DataReader dataReader;
  private GuardCondition guardCondition;
  private ReadCondition readCondition;
  private WaitSet waitSet;

  private ExecutorService executorService;

  private OnDataAvailableListener<T> listener;
  private DataReaderWatcherExecutor<T> executor;

  public DataReaderWatcher(
    DataReader dataReader,
    ReadConditionParams readConditionParams,
    DataReaderWatcherExecutor<T> executor,
    OnDataAvailableListener<T> listener
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
    // only allocate native sequence once
    var conditionSeq = new ConditionSeq();
    conditionSeq.setMaximum(2);

    do {
      // wait until condition is triggered
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
