/*
 * MIT License
 *
 * Copyright (c) 2018 Andreas Guther
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

import static com.google.common.base.Preconditions.checkNotNull;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_NOT_ENABLED;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.ViewStateKind;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicDataSeq;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicDataTypeSupport;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements an observer for subscriptions.
 */
public class SubscriptionObserver extends BuiltinTopicObserver {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionObserver.class);

  private final Map<InstanceHandle_t, SubscriptionBuiltinTopicData> sampleCache;
  private final Set<SubscriptionObserverListener> listeners;

  /**
   * Creates a new observer for subscriptions.
   *
   * @param domainParticipant the domain participant (that is not yet enabled)
   */
  public SubscriptionObserver(
      final DomainParticipant domainParticipant
  ) {
    // create the parent observer with the built-in subscription topic
    super(domainParticipant, SubscriptionBuiltinTopicDataTypeSupport.SUBSCRIPTION_TOPIC_NAME);

    // initialize sample cache
    sampleCache = Collections.synchronizedMap(new HashMap<>());

    // create set for listeners with lock
    listeners = Collections.synchronizedSet(new HashSet<>());
  }

  @Override
  public void close() {
    listeners.clear();
    super.close();
  }

  /**
   * Add listener.
   *
   * @param listener the listener
   */
  public void addListener(
      final SubscriptionObserverListener listener
  ) {
    addListener(listener, true);
  }

  /**
   * Add listener.
   *
   * @param listener the listener
   * @param deliverReadSamples true to deliver already read samples
   */
  public void addListener(
      final SubscriptionObserverListener listener,
      final boolean deliverReadSamples
  ) {
    checkNotNull(listener, "Listener must not be null");

    synchronized (listeners) {
      listeners.add(listener);

      if (deliverReadSamples) {
        deliverReadSamples(listener);
      }
    }
  }

  /**
   * Remove listener.
   *
   * @param listener the listener
   */
  public void removeListener(
      final SubscriptionObserverListener listener
  ) {
    checkNotNull(listener, "Listener must not be null");
    listeners.remove(listener);
  }

  @Override
  public void run() {
    do {
      try {
        // create data containers
        SubscriptionBuiltinTopicData sample = new SubscriptionBuiltinTopicData();
        SampleInfo sampleInfo = new SampleInfo();

        // read next sample
        dataReader.read_next_sample_untyped(sample, sampleInfo);

        if (sampleInfo.valid_data) {
          // decide if subscription was modified or discovered
          boolean discovered = !sampleCache.containsKey(sampleInfo.instance_handle);

          // cache sample for the lost event
          sampleCache.put(sampleInfo.instance_handle, sample);

          // call listeners
          synchronized (listeners) {
            if (discovered) {
              invokeSubscriptionDiscovered(sample, sampleInfo);
            } else {
              invokeSubscriptionModified(sample, sampleInfo);
            }
          }
        } else if (sampleInfo.instance_state != InstanceStateKind.ALIVE_INSTANCE_STATE) {
          // get sample from cached data
          sample = sampleCache.remove(sampleInfo.instance_handle);

          // call listeners
          synchronized (listeners) {
            invokeSubscriptionLost(sample, sampleInfo);
          }
        }
      } catch (RETCODE_NO_DATA noData) {
        LOGGER.trace("No more data available to read");
        return;
      } catch (RETCODE_ERROR error) {
        LOGGER.error("Error reading sample; {}", error);
        return;
      }
    } while (true);
  }

  /**
   * Informs the listeners about the discovery of a subscription.
   *
   * @param sample subscription data
   * @param sampleInfo subscription info
   */
  private void invokeSubscriptionDiscovered(
      SubscriptionBuiltinTopicData sample,
      SampleInfo sampleInfo
  ) {
    // log information
    logListenerInvocation("subscriptionDiscovered", sampleInfo, sample);

    // iterate over listeners and invoke them
    for (SubscriptionObserverListener listener : listeners) {
      listener.subscriptionDiscovered(
          domainParticipant,
          sampleInfo.instance_handle,
          sample
      );
    }
  }

  /**
   * Informs the listeners about the modification of a subscription.
   *
   * @param sample subscription data
   * @param sampleInfo subscription info
   */
  private void invokeSubscriptionModified(
      SubscriptionBuiltinTopicData sample,
      SampleInfo sampleInfo
  ) {
    // log information
    logListenerInvocation("subscriptionModified", sampleInfo, sample);

    // iterate over listeners and invoke them
    for (SubscriptionObserverListener listener : listeners) {
      listener.subscriptionModified(
          domainParticipant,
          sampleInfo.instance_handle,
          sample
      );
    }
  }

  /**
   * Informs the listeners about the loss of a subscription.
   *
   * @param sample subscription data
   * @param sampleInfo subscription info
   */
  private void invokeSubscriptionLost(
      SubscriptionBuiltinTopicData sample,
      SampleInfo sampleInfo
  ) {
    // log information
    logListenerInvocation("subscriptionLost", sampleInfo, sample);

    // iterate over listeners and invoke them
    for (SubscriptionObserverListener listener : listeners) {
      listener.subscriptionLost(
          domainParticipant,
          sampleInfo.instance_handle,
          sample
      );
    }
  }

  /**
   * Reads and delivers already read samples for new listeners.
   *
   * @param listener the listener
   */
  private void deliverReadSamples(
      final SubscriptionObserverListener listener
  ) {
    // variables to store data
    SampleInfo sampleInfo = new SampleInfo();
    SampleInfoSeq sampleInfoSeq = new SampleInfoSeq();
    SubscriptionBuiltinTopicDataSeq sampleSeq = new SubscriptionBuiltinTopicDataSeq();

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

          // subscription data does not need copy
          SubscriptionBuiltinTopicData sample = (SubscriptionBuiltinTopicData) sampleSeq.get(i);

          // invoke listener if provided
          listener.subscriptionDiscovered(
              domainParticipant,
              sampleInfo.instance_handle,
              sample
          );
        }
      }
    } catch (RETCODE_NOT_ENABLED notEnabled) {
      // yet there is no way to directly detect that the domain participant is not yet enabled
      // and therefore this error is expected when a listener is added but the related domain participant
      // is not yet enabled
    } catch (RETCODE_NO_DATA noData) {
      LOGGER.trace("No more data available to read");
    } catch (RETCODE_ERROR error) {
      LOGGER.error("Error getting already read samples; {}", error);
    } finally {
      dataReader.return_loan_untyped(sampleSeq, sampleInfoSeq);
    }
  }

  private void logListenerInvocation(
      String name,
      SampleInfo sampleInfo,
      SubscriptionBuiltinTopicData sample
  ) {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace(
          "Calling '{}' on listeners with instance='{}', topic='{}', type='{}', sampleInfo='{}', sample='{}'",
          name,
          sampleInfo.instance_handle,
          sample.topic_name,
          sample.type_name,
          sampleInfo.toString().replace("\n", "").replaceAll("[ ]{2,}", " "),
          sample.toString().replace("\n", "").replaceAll("[ ]{2,}", " ")
      );
    } else if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "Calling '{}' on listeners with instance='{}', topic='{}', type='{}'",
          name,
          sampleInfo.instance_handle,
          sample.topic_name,
          sample.type_name
      );
    }
  }
}
