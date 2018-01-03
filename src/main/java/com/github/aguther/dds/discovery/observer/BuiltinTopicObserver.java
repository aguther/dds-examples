/*
 * MIT License
 *
 * Copyright (c) 2017 Andreas Guther
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

package com.github.aguther.dds.discovery.observer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Strings;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allows the observation of a built-in topic.
 * Built-in topics are used to track discovery information like domain participants, publications and subscriptions.
 */
class BuiltinTopicObserver extends DataReaderAdapter implements Closeable, Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuiltinTopicObserver.class);

  private final ExecutorService executorService;

  final DomainParticipant domainParticipant;
  final DataReader dataReader;

  /**
   * Instantiates a new Builtin topic observer.
   *
   * @param domainParticipant not enabled domain participant (otherwise data will be missed)
   * @param topicName the topic name
   */
  BuiltinTopicObserver(
      final DomainParticipant domainParticipant,
      final String topicName
  ) {
    // check arguments (we do not check if domain participant is enabled because it triggers an error log
    checkNotNull(domainParticipant, "DomainParticipant must not be null");
    checkArgument(!Strings.isNullOrEmpty(topicName), "Topic name must not be empty or null");

    // keep reference to domain participant
    this.domainParticipant = domainParticipant;

    // get the data reader of the topic
    dataReader = domainParticipant.get_builtin_subscriber().lookup_datareader(topicName);
    checkNotNull(dataReader, "Failed to get data reader for topic '%s'", topicName);

    // set listener on data reader so we get the data
    dataReader.set_listener(this, StatusKind.DATA_AVAILABLE_STATUS);

    // create executor as single thread
    executorService = Executors.newSingleThreadExecutor();
  }

  /**
   * Clean-up resources of this object, in this case remove the listener from
   * the data reader we are bound to and shutdown the executor service.
   */
  public void close() {
    // remove listener from data reader
    dataReader.set_listener(null, StatusKind.STATUS_MASK_NONE);
    // shutdown executor
    executorService.shutdownNow();
  }

  /**
   * This function is invoked by the middleware in a special thread whenever data is available.
   * For this reason, it must not be blocked.
   *
   * Reading data should be done in the run method that is being invoked in a separate thread.
   */
  @Override
  public void on_data_available(
      final DataReader dataReader
  ) {
    // here we get the information that data is available
    // now we need to inform our listeners that something new has been discovered
    LOGGER.trace("Method 'on_data_available' invoked.");
    // trigger to read samples
    executorService.submit(this);
  }

  @Override
  public void run() {
    // override this method to read data
    LOGGER.trace("Method 'run' invoked.");
  }
}
