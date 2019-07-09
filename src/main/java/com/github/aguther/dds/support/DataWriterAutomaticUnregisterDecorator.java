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

package com.github.aguther.dds.support;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.publication.DataWriter;
import com.rti.dds.publication.DataWriterQos;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataWriterAutomaticUnregisterDecorator extends DataWriterDecorator implements Closeable {

  private long unregisterTimeout;
  private ScheduledExecutorService scheduledExecutorService;
  private HashMap<InstanceHandle_t, Future> futureHashMap;

  private static class UnregisterRunnable implements Runnable {

    private DataWriter dataWriter;
    private InstanceHandle_t instanceHandle;

    private UnregisterRunnable(
      DataWriter dataWriter,
      InstanceHandle_t instanceHandle
    ) {
      checkNotNull(dataWriter);
      checkArgument(instanceHandle != InstanceHandle_t.HANDLE_NIL);

      this.dataWriter = dataWriter;
      this.instanceHandle = instanceHandle;
    }

    @Override
    public void run() {
      // unregister the instance
      dataWriter.unregister_instance_untyped(null, instanceHandle);
    }
  }

  public DataWriterAutomaticUnregisterDecorator(
    DataWriter dataWriter
  ) {
    // initialize super class
    super(dataWriter);

    // detect lifespan to specify unregister interval
    DataWriterQos dataWriterQos = new DataWriterQos();
    dataWriter.get_qos(dataWriterQos);
    unregisterTimeout = TimeUnit.SECONDS.toMillis(dataWriterQos.lifespan.duration.sec);
    unregisterTimeout += TimeUnit.NANOSECONDS.toMillis(dataWriterQos.lifespan.duration.nanosec);
    unregisterTimeout *= 1.05;

    // create executor service
    scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    // create map for futures
    futureHashMap = new HashMap<>();
  }

  /**
   * Closes this stream and releases any system resources associated
   * with it. If the stream is already closed then invoking this
   * method has no effect.
   *
   * <p> As noted in {@link AutoCloseable#close()}, cases where the
   * close may fail require careful attention. It is strongly advised
   * to relinquish the underlying resources and to internally
   * <em>mark</em> the {@code Closeable} as closed, prior to throwing
   * the {@code IOException}.
   *
   * @throws IOException if an I/O error occurs
   */
  @Override
  public void close() throws IOException {
    if (scheduledExecutorService != null) {
      scheduledExecutorService.shutdownNow();
      scheduledExecutorService = null;
    }
  }

  @Override
  public void unregister_instance_untyped(
    Object o,
    InstanceHandle_t instanceHandle
  ) {
    // unregister the instance
    super.unregister_instance_untyped(o, instanceHandle);

    // remove any future
    futureHashMap.remove(instanceHandle);
  }

  @Override
  public void write_untyped(
    Object o,
    InstanceHandle_t instanceHandle
  ) {
    // ensure parameters are not null
    checkNotNull(o);
    checkNotNull(instanceHandle);

    // register or get handle if not provided
    if (instanceHandle == InstanceHandle_t.HANDLE_NIL) {
      instanceHandle = register_instance_untyped(o);
    }

    // check if an unregister was already scheduled
    if (futureHashMap.containsKey(instanceHandle)) {
      futureHashMap.remove(instanceHandle).cancel(false);
    }

    // write sample
    super.write_untyped(o, instanceHandle);

    // schedule unregister
    Future future = scheduledExecutorService.schedule(
      new UnregisterRunnable(this, instanceHandle),
      unregisterTimeout,
      TimeUnit.MILLISECONDS
    );

    // store instance handle in keymap
    futureHashMap.put(instanceHandle, future);
  }
}
