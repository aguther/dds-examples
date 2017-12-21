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

import static com.google.common.base.Preconditions.checkNotNull;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_NOT_ENABLED;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicDataSeq;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicDataTypeSupport;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.ViewStateKind;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements an observer for publications.
 */
public class PublicationObserver extends BuiltinTopicObserver implements Runnable {

  private static final Logger log;

  static {
    log = LoggerFactory.getLogger(PublicationObserver.class);
  }

  private final Map<InstanceHandle_t, PublicationBuiltinTopicData> sampleCache;
  private final List<PublicationObserverListener> listenerList;

  /**
   * Creates a new observer for publications.
   *
   * @param domainParticipant DomainParticipant to use
   * @throws IllegalArgumentException Thrown in case of an error
   */
  public PublicationObserver(
      DomainParticipant domainParticipant) {

    // create the parent observer with the built-in publication topic
    super(domainParticipant, PublicationBuiltinTopicDataTypeSupport.PUBLICATION_TOPIC_NAME);

    // initialize sample cache
    sampleCache = Collections.synchronizedMap(new HashMap<>());

    // create list for listenerList with lock
    listenerList = Collections.synchronizedList(new ArrayList<>());
  }

  public void addListener(
      PublicationObserverListener listener
  ) {
    addListener(listener, true);
  }

  public void addListener(
      PublicationObserverListener listener,
      boolean deliverReadSamples
  ) {
    checkNotNull(listener, "Listener must not be null");

    synchronized (listenerList) {
      if (!listenerList.contains(listener)) {
        listenerList.add(listener);
        if (deliverReadSamples) {
          deliverReadSamples(listener);
        }
      }
    }
  }

  public void removeListener(
      PublicationObserverListener listener
  ) {
    checkNotNull(listener, "Listener must not be null");
    listenerList.remove(listener);
  }

  @Override
  public void run() {
    boolean hasMoreData = true;

    do {
      try {
        // create data containers
        PublicationBuiltinTopicData sample = new PublicationBuiltinTopicData();
        SampleInfo sampleInfo = new SampleInfo();

        // read next sample
        dataReader.read_next_sample_untyped(sample, sampleInfo);

        if (sampleInfo.valid_data) {
          // cache sample for the lost event
          sampleCache.put(sampleInfo.instance_handle, sample);

          // call listeners
          synchronized (listenerList) {
            for (PublicationObserverListener listener : listenerList) {
              listener.publicationDiscovered(sampleInfo.instance_handle, sample);
            }
          }
        } else if (sampleInfo.instance_state != InstanceStateKind.ALIVE_INSTANCE_STATE) {
          // get sample from cached data
          sample = sampleCache.remove(sampleInfo.instance_handle);

          // call listeners
          synchronized (listenerList) {
            for (PublicationObserverListener listener : listenerList) {
              listener.publicationLost(sampleInfo.instance_handle, sample);
            }
          }
        }
      } catch (RETCODE_NO_DATA exception) {
        hasMoreData = false;
      }

    } while (hasMoreData);
  }

  private void deliverReadSamples(
      PublicationObserverListener listener
  ) {
    // variables to store data
    SampleInfo sampleInfo = new SampleInfo();
    SampleInfoSeq sampleInfoSeq = new SampleInfoSeq();
    PublicationBuiltinTopicDataSeq sampleSeq = new PublicationBuiltinTopicDataSeq();

    try {
      // read samples that have already been read
      dataReader.read_untyped(
          sampleSeq,
          sampleInfoSeq,
          Integer.MAX_VALUE,
          SampleStateKind.READ_SAMPLE_STATE,
          ViewStateKind.ANY_VIEW_STATE,
          InstanceStateKind.ANY_INSTANCE_STATE
      );

      // iterate over samples
      for (int i = 0; i < sampleSeq.size(); i++) {
        // is data valid?
        if (sampleInfoSeq.get(i).valid_data) {
          // copy sample info
          sampleInfo.copy_from(sampleInfoSeq.get(i));

          // publication data does not need copy
          PublicationBuiltinTopicData sample = (PublicationBuiltinTopicData) sampleSeq.get(i);

          // invoke listener
          listener.publicationDiscovered(sampleInfo.instance_handle, sample);
        }
      }
    } catch (RETCODE_NOT_ENABLED notEnabled) {
      // yet there is no way to directly detect that the domain participant is not yet enabled
      // and therefore this error is expected when a listener is added but the related domain participant
      // is not yet enabled
    } catch (RETCODE_NO_DATA noData) {
      if (log.isTraceEnabled()) {
        log.trace("No more data available to read; {}", noData);
      }
    } catch (RETCODE_ERROR error) {
      log.error("Error getting already read samples; {}", error);
    } finally {
      dataReader.return_loan_untyped(sampleSeq, sampleInfoSeq);
    }
  }
}
