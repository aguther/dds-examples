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

package com.github.aguther.dds.examples.discovery;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicDataTypeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements an observer for subscriptions.
 */
public class SubscriptionObserver extends BuiltinTopicObserver {

  private static final Logger log;

  static {
    log = LoggerFactory.getLogger(Discovery.class);
  }

  private final HashMap<InstanceHandle_t, SubscriptionBuiltinTopicData> sampleCache;

  private final Object listenerLock;
  private final List<SubscriptionObserverListener> listenerList;

  /**
   * Creates a new observer for subscriptions.
   *
   * @param domainParticipant DomainParticipant to use
   * @throws IllegalArgumentException Thrown in case of an error
   */
  public SubscriptionObserver(
      DomainParticipant domainParticipant) {
    // create the parent observer with the built-in subscription topic
    super(domainParticipant, SubscriptionBuiltinTopicDataTypeSupport.SUBSCRIPTION_TOPIC_NAME);

    // initialize sample cache
    sampleCache = new HashMap<>();

    // create list for listenerList with lock
    listenerLock = new Object();
    listenerList = new ArrayList<>();
  }

  public void addListener(SubscriptionObserverListener listener) {
    synchronized (listenerLock) {
      if (!listenerList.contains(listener)) {
        listenerList.add(listener);
      }
    }
  }

  public void removeListener(SubscriptionObserverListener listener) {
    synchronized (listenerLock) {
      listenerList.remove(listener);
    }
  }

  @Override
  public void run() {
    boolean hasMoreData = true;

    do {
      try {
        // create data containers
        SubscriptionBuiltinTopicData sample = new SubscriptionBuiltinTopicData();
        SampleInfo sampleInfo = new SampleInfo();

        // read next sample
        dataReader.read_next_sample_untyped(sample, sampleInfo);

        if (sampleInfo.valid_data) {
          // cache sample for the lost event
          sampleCache.put(sampleInfo.instance_handle, sample);

          // call listeners
          synchronized (listenerLock) {
            for (SubscriptionObserverListener listener : listenerList) {
              listener.subscriptionDiscovered(sampleInfo.instance_handle, sample);
            }
          }
        } else if (sampleInfo.instance_state != InstanceStateKind.ALIVE_INSTANCE_STATE) {
          // get sample from cached data
          sample = sampleCache.remove(sampleInfo.instance_handle);

          // call listeners
          synchronized (listenerLock) {
            for (SubscriptionObserverListener listener : listenerList) {
              listener.subscriptionLost(sampleInfo.instance_handle, sample);
            }
          }
        }
      } catch (RETCODE_NO_DATA exception) {
        hasMoreData = false;
      }

    } while (hasMoreData);
  }
}
