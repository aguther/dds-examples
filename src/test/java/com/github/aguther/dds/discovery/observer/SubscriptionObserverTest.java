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

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.InstanceHandleSeq;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_NOT_ENABLED;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.ViewStateKind;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicDataSeq;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SubscriptionObserverTest {

  private DataReader dataReader;
  private SubscriptionObserver subscriptionObserver;
  private SubscriptionObserverListener subscriptionObserverListener;

  @Before
  public void setUp() {
    DomainParticipant domainParticipant = mock(DomainParticipant.class);
    Subscriber subscriber = mock(Subscriber.class);
    dataReader = mock(DataReader.class);

    doThrow(new RETCODE_NOT_ENABLED()).when(domainParticipant).get_discovered_participants(new InstanceHandleSeq());
    when(domainParticipant.get_builtin_subscriber()).thenReturn(subscriber);
    when(subscriber.lookup_datareader(anyString()))
        .thenReturn(dataReader);

    subscriptionObserver = new SubscriptionObserver(domainParticipant);

    subscriptionObserverListener = mock(SubscriptionObserverListener.class);
    subscriptionObserver.addListener(subscriptionObserverListener);
  }

  @After
  public void tearDown() {
    subscriptionObserver.removeListener(subscriptionObserverListener);
    subscriptionObserver.close();
  }

  @Test
  public void testInstantiation() {
    assertNotNull(subscriptionObserver);
  }

  @Test(timeout = 10000)
  public void testRun() {
    // prepare answers
    doAnswer(
        invocation -> {
          SampleInfo sampleInfo = invocation.getArgument(1);
          sampleInfo.valid_data = true;
          sampleInfo.instance_state = InstanceStateKind.ALIVE_INSTANCE_STATE;
          return null;
        }
    ).doAnswer(
        invocation -> {
          SampleInfo sampleInfo = invocation.getArgument(1);
          sampleInfo.valid_data = false;
          sampleInfo.instance_state = InstanceStateKind.NOT_ALIVE_INSTANCE_STATE;
          return null;
        }
    ).doThrow(new RETCODE_NO_DATA()
    ).when(dataReader).read_next_sample_untyped(
        new SubscriptionBuiltinTopicData(),
        new SampleInfo()
    );

    // execute tested method
    subscriptionObserver.run();

    // verify results
    verify(subscriptionObserverListener, times(1)).subscriptionDiscovered(
        any(DomainParticipant.class),
        any(InstanceHandle_t.class),
        any(SubscriptionBuiltinTopicData.class));

    verify(subscriptionObserverListener, times(1)).subscriptionLost(
        any(DomainParticipant.class),
        any(InstanceHandle_t.class),
        any(SubscriptionBuiltinTopicData.class));
  }

  @Test(timeout = 10000)
  public void testRunError() {
    // prepare answers
    doThrow(new RETCODE_ERROR()
    ).when(dataReader).read_next_sample_untyped(
        new SubscriptionBuiltinTopicData(),
        new SampleInfo()
    );

    // execute tested method
    subscriptionObserver.run();

    // verify results
    verify(subscriptionObserverListener, times(0)).subscriptionDiscovered(
        any(DomainParticipant.class),
        any(InstanceHandle_t.class),
        any(SubscriptionBuiltinTopicData.class));

    verify(subscriptionObserverListener, times(0)).subscriptionLost(
        any(DomainParticipant.class),
        any(InstanceHandle_t.class),
        any(SubscriptionBuiltinTopicData.class));
  }

  @Test(timeout = 10000)
  public void testDeliverReadSamples() {

    // add another listener
    SubscriptionObserverListener listener = mock(SubscriptionObserverListener.class);

    // prepare answers
    doAnswer(
        invocation -> {
          SubscriptionBuiltinTopicDataSeq sampleSeq = invocation.getArgument(0);
          SampleInfoSeq sampleInfoSeq = invocation.getArgument(1);

          SubscriptionBuiltinTopicData subscriptionBuiltinTopicData = new SubscriptionBuiltinTopicData();
          sampleSeq.add(subscriptionBuiltinTopicData);

          SampleInfo sampleInfo = new SampleInfo();
          sampleInfo.valid_data = true;
          sampleInfoSeq.add(sampleInfo);
          return null;
        }
    ).doThrow(new RETCODE_NO_DATA()
    ).when(dataReader).read_untyped(
        new SubscriptionBuiltinTopicDataSeq(),
        new SampleInfoSeq(),
        Integer.MAX_VALUE,
        SampleStateKind.READ_SAMPLE_STATE,
        ViewStateKind.ANY_VIEW_STATE,
        InstanceStateKind.ANY_INSTANCE_STATE
    );

    // execute tested method
    subscriptionObserver.addListener(listener);

    // verify results
    verify(subscriptionObserverListener, times(0)).subscriptionDiscovered(
        any(DomainParticipant.class),
        any(InstanceHandle_t.class),
        any(SubscriptionBuiltinTopicData.class));
    verify(subscriptionObserverListener, times(0)).subscriptionLost(
        any(DomainParticipant.class),
        any(InstanceHandle_t.class),
        any(SubscriptionBuiltinTopicData.class));
    verify(listener, times(1)).subscriptionDiscovered(
        any(DomainParticipant.class),
        any(InstanceHandle_t.class),
        any(SubscriptionBuiltinTopicData.class));
    verify(listener, times(0)).subscriptionLost(
        any(DomainParticipant.class),
        any(InstanceHandle_t.class),
        any(SubscriptionBuiltinTopicData.class));
  }

  @Test(timeout = 10000)
  public void testDeliverReadSamplesNotEnabled() {
    testDeliverReadSamplesWithException(new RETCODE_NOT_ENABLED());
  }

  @Test(timeout = 10000)
  public void testDeliverReadSamplesNoData() {
    testDeliverReadSamplesWithException(new RETCODE_NO_DATA());
  }

  @Test(timeout = 10000)
  public void testDeliverReadSamplesError() {
    testDeliverReadSamplesWithException(new RETCODE_ERROR());
  }

  private void testDeliverReadSamplesWithException(
      RETCODE_ERROR exception
  ) {
    // add another listener
    SubscriptionObserverListener listener = mock(SubscriptionObserverListener.class);

    // prepare answers
    doThrow(exception
    ).when(dataReader).read_untyped(
        new SubscriptionBuiltinTopicDataSeq(),
        new SampleInfoSeq(),
        Integer.MAX_VALUE,
        SampleStateKind.READ_SAMPLE_STATE,
        ViewStateKind.ANY_VIEW_STATE,
        InstanceStateKind.ANY_INSTANCE_STATE
    );

    // execute tested method
    subscriptionObserver.addListener(listener);

    // verify results
    verify(subscriptionObserverListener, times(0)).subscriptionDiscovered(
        any(DomainParticipant.class),
        any(InstanceHandle_t.class),
        any(SubscriptionBuiltinTopicData.class));
    verify(subscriptionObserverListener, times(0)).subscriptionLost(
        any(DomainParticipant.class),
        any(InstanceHandle_t.class),
        any(SubscriptionBuiltinTopicData.class));
    verify(listener, times(0)).subscriptionDiscovered(
        any(DomainParticipant.class),
        any(InstanceHandle_t.class),
        any(SubscriptionBuiltinTopicData.class));
    verify(listener, times(0)).subscriptionLost(
        any(DomainParticipant.class),
        any(InstanceHandle_t.class),
        any(SubscriptionBuiltinTopicData.class));
  }
}
