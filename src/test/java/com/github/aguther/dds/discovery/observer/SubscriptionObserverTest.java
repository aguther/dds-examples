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

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicDataSeq;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicDataTypeSupport;
import com.rti.dds.util.LoanableSequence;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    BuiltinTopicObserver.class,
    DomainParticipant.class,
    LoanableSequence.class,
    SubscriptionBuiltinTopicData.class,
    SubscriptionBuiltinTopicDataSeq.class,
    SubscriptionBuiltinTopicDataTypeSupport.class,
    SubscriptionObserver.class,
    SampleInfo.class,
    SampleInfoSeq.class,
})
@SuppressStaticInitializationFor({
    "com.rti.dds.domain.builtin.ParticipantBuiltinTopicDataTypeSupport",
    "com.rti.dds.domain.DomainParticipant",
    "com.rti.dds.publication.builtin.PublicationBuiltinTopicData",
    "com.rti.dds.publication.builtin.PublicationBuiltinTopicDataTypeSupport",
    "com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData",
    "com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicDataSeq",
    "com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicDataTypeSupport",
    "com.rti.dds.subscription.SampleInfoSeq",
    "com.rti.dds.topic.AbstractBuiltinTopicData",
    "com.rti.dds.topic.builtin.ServiceRequestTypeSupport",
    "com.rti.dds.topic.builtin.TopicBuiltinTopicDataTypeSupport",
    "com.rti.dds.topic.builtin.TopicBuiltinTopicDataTypeSupport",
    "com.rti.dds.topic.TypeSupportImpl",
    "com.rti.dds.util.LoanableSequence",
})
public class SubscriptionObserverTest {

  private DataReader dataReader;
  private SubscriptionObserver subscriptionObserver;
  private SubscriptionObserverListener subscriptionObserverListener;

  private SubscriptionBuiltinTopicData subscriptionBuiltinTopicData;
  private SubscriptionBuiltinTopicDataSeq subscriptionBuiltinTopicDataSeq;

  private SampleInfo sampleInfo;
  private SampleInfoSeq sampleInfoSeq;

  @Before
  public void setUp() throws Exception {
    DomainParticipant domainParticipant = mock(DomainParticipant.class);
    Subscriber subscriber = mock(Subscriber.class);
    dataReader = mock(DataReader.class);

    Whitebox.setInternalState(
        SubscriptionBuiltinTopicDataTypeSupport.class,
        "SUBSCRIPTION_TOPIC_NAME",
        "SubscriptionBuiltinTopicName"
    );

    subscriptionBuiltinTopicData = mock(SubscriptionBuiltinTopicData.class);
    subscriptionBuiltinTopicData.topic_name = "Square";
    subscriptionBuiltinTopicData.type_name = "ShapeType";
    PowerMockito.whenNew(SubscriptionBuiltinTopicData.class).withAnyArguments().thenReturn(
        subscriptionBuiltinTopicData);

    subscriptionBuiltinTopicDataSeq = mock(SubscriptionBuiltinTopicDataSeq.class);
    PowerMockito.whenNew(SubscriptionBuiltinTopicDataSeq.class).withAnyArguments().thenReturn(
        subscriptionBuiltinTopicDataSeq);

    sampleInfo = mock(SampleInfo.class);
    Whitebox.setInternalState(sampleInfo, "instance_handle", InstanceHandle_t.HANDLE_NIL);
    PowerMockito.whenNew(SampleInfo.class).withAnyArguments().thenReturn(sampleInfo);

    sampleInfoSeq = mock(SampleInfoSeq.class);
    PowerMockito.whenNew(SampleInfoSeq.class).withAnyArguments().thenReturn(sampleInfoSeq);

    when(domainParticipant.get_builtin_subscriber()).thenReturn(subscriber);
    when(subscriber.lookup_datareader(anyString()))
        .thenReturn(dataReader);

    subscriptionObserver = new SubscriptionObserver(domainParticipant);

    subscriptionObserverListener = mock(SubscriptionObserverListener.class);
    subscriptionObserver.addListener(subscriptionObserverListener, false);
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
  public void testRunWithLevelDebug() {
    // remember current level
    Level originalLevel = LogManager.getRootLogger().getLevel();
    try {
      // set logging to DEBUG
      Configurator.setRootLevel(Level.DEBUG);

      // execute run method
      testRun();

    } finally {
      // restore level
      Configurator.setRootLevel(originalLevel);
    }
  }

  @Test(timeout = 10000)
  public void testRunWithLevelTrace() {
    // remember current level
    Level originalLevel = LogManager.getRootLogger().getLevel();
    try {
      // set logging to TRACE
      Configurator.setRootLevel(Level.TRACE);

      // execute run method
      testRun();

    } finally {
      // restore level
      Configurator.setRootLevel(originalLevel);
    }
  }

  private void testRun() {
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
        eq(subscriptionBuiltinTopicData),
        eq(sampleInfo)
    );

    // execute tested method
    subscriptionObserver.run();

    // verify results
    verify(subscriptionObserverListener, times(1)).subscriptionDiscovered(
        any(DomainParticipant.class),
        any(InstanceHandle_t.class),
        any(SubscriptionBuiltinTopicData.class));

    verify(subscriptionObserverListener, times(1)).subscriptionModified(
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
        eq(subscriptionBuiltinTopicData),
        eq(sampleInfo)
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
  public void testAddListenerWithReadSamples() {
    // create another listener
    SubscriptionObserverListener listener = mock(SubscriptionObserverListener.class);

    // prepare answers
    doAnswer(
        invocation -> {
          SampleInfo sampleInfo = invocation.getArgument(1);
          sampleInfo.valid_data = true;
          sampleInfo.instance_state = InstanceStateKind.ALIVE_INSTANCE_STATE;
          return null;
        }
    ).doThrow(new RETCODE_NO_DATA()
    ).when(dataReader).read_next_sample_untyped(
        eq(subscriptionBuiltinTopicData),
        eq(sampleInfo)
    );

    // execute run method so sample is stored in cache
    subscriptionObserver.run();

    // execute tested method
    subscriptionObserver.addListener(listener);

    // verify results
    verify(subscriptionObserverListener, times(1)).subscriptionDiscovered(
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
}
