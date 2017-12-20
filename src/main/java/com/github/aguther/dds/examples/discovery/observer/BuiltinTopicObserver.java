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

package com.github.aguther.dds.examples.discovery.observer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Strings;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.builtin.ParticipantBuiltinTopicData;
import com.rti.dds.infrastructure.InstanceHandleSeq;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BuiltinTopicObserver extends DataReaderAdapter implements Runnable {

  private static final Logger log;

  static {
    log = LoggerFactory.getLogger(BuiltinTopicObserver.class);
  }

  private ExecutorService executorService;
  private DomainParticipant domainParticipant;
  DataReader dataReader;

  /**
   * Creates a new discovery observer.
   *
   * @param domainParticipant DomainParticipant to bind to.
   * @param topicName Topic that should be observed.
   * @throws IllegalArgumentException Whenever an error occurs.
   */
  BuiltinTopicObserver(
      final DomainParticipant domainParticipant,
      final String topicName) {
    // check arguments
    checkNotNull(domainParticipant);
    checkArgument(!Strings.isNullOrEmpty(topicName));

    // remember domain participant
    this.domainParticipant = domainParticipant;

    // get the data reader of the topic
    dataReader = domainParticipant.get_builtin_subscriber().lookup_datareader(topicName);
    if (dataReader == null) {
      throw new IllegalArgumentException(String.format("Failed to get data reader for topic '%s'", topicName));
    }

    // set listener on data reader so we get the data
    dataReader.set_listener(this, StatusKind.DATA_AVAILABLE_STATUS);

    // create executor as single thread
    executorService = Executors.newSingleThreadExecutor();
  }

  /**
   * Clean-up resources of this object, in this case remove the listener from the data reader we
   * are bound to.
   */
  public void close() {
    // remove listener from data reader
    dataReader.set_listener(null, StatusKind.STATUS_MASK_NONE);
  }

  /**
   * Get the participant data for a given publication data.
   *
   * @param publicationBuiltinTopicData Publication data
   * @return If found, the participant data otherwise null.
   */
  ParticipantBuiltinTopicData getParticipantBuiltinTopicDataFromPublication(
      PublicationBuiltinTopicData publicationBuiltinTopicData
  ) {
    // instance handle sequence
    InstanceHandleSeq handles = new InstanceHandleSeq();

    // get discovered participants
    domainParticipant.get_discovered_participants(handles);

    for (Object handleObject : handles) {
      // create data object
      ParticipantBuiltinTopicData participantData = new ParticipantBuiltinTopicData();

      // get participant data for instance
      domainParticipant.get_discovered_participant_data(
          participantData,
          (InstanceHandle_t) handleObject);

      // check if this is the participant we are searching for
      if (participantData.key.equals(publicationBuiltinTopicData.participant_key)) {
        return participantData;
      }
    }

    // we did not find the participant data
    return null;
  }

  @Override
  public void on_data_available(
      DataReader dataReader
  ) {
    // here we get the information that data is available
    // now we need to inform our listeners that something new has been discovered
    log.trace("Method 'on_data_available' invoked.");
    // trigger to read samples
    executorService.execute(this);
  }

  @Override
  public void run() {
    // override this method to read data
  }
}
