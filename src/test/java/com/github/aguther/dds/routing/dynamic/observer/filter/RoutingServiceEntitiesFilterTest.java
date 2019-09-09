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

package com.github.aguther.dds.routing.dynamic.observer.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.aguther.dds.util.BuiltinTopicHelper;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.builtin.ParticipantBuiltinTopicData;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.ServiceQosPolicy;
import com.rti.dds.infrastructure.ServiceQosPolicyKind;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import com.rti.dds.topic.BuiltinTopicKey_t;
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
  RoutingServiceEntitiesFilter.class,
  DomainParticipant.class,
  ServiceQosPolicy.class,
  ParticipantBuiltinTopicData.class,
  PublicationBuiltinTopicData.class,
  SubscriptionBuiltinTopicData.class,
  InstanceHandle_t.class,
  BuiltinTopicKey_t.class,
  BuiltinTopicHelper.class
})
@SuppressStaticInitializationFor({
  "com.rti.dds.domain.DomainParticipant",
  "com.rti.dds.domain.builtin.ParticipantBuiltinTopicDataTypeSupport",
  "com.rti.dds.topic.BuiltinTopicKey_t",
  "com.rti.dds.topic.TypeSupportImpl",
  "com.rti.dds.topic.builtin.ServiceRequestTypeSupport",
  "com.rti.dds.topic.builtin.TopicBuiltinTopicDataTypeSupport",
  "com.rti.dds.infrastructure.InstanceHandle_t",
  "com.rti.dds.publication.builtin.PublicationBuiltinTopicDataTypeSupport",
  "com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicDataTypeSupport"
})
public class RoutingServiceEntitiesFilterTest {

  private DomainParticipant domainParticipant;
  private RoutingServiceEntitiesFilter filter;

  @Before
  public void setUp() {
    domainParticipant = mock(DomainParticipant.class);
    filter = new RoutingServiceEntitiesFilter();
  }

  @After
  public void tearDown() {
    domainParticipant = null;
    filter = null;
  }

  @Test
  public void testIgnorePublicationTrue() throws Exception {
    // setup mock for participant data
    ParticipantBuiltinTopicData participantBuiltinTopicData = mock(ParticipantBuiltinTopicData.class);
    {
      ServiceQosPolicy serviceQosPolicy = PowerMockito.mock(ServiceQosPolicy.class);
      serviceQosPolicy.kind = ServiceQosPolicyKind.ROUTING_SERVICE_QOS;
      Whitebox.setInternalState(participantBuiltinTopicData, "service", serviceQosPolicy);
    }
    PowerMockito.whenNew(ParticipantBuiltinTopicData.class).withAnyArguments().thenReturn(participantBuiltinTopicData);

    // setup mock for topic helper
    PowerMockito.mockStatic(BuiltinTopicHelper.class);
    when(BuiltinTopicHelper.getParticipantBuiltinTopicData(
      any(DomainParticipant.class), any(BuiltinTopicKey_t.class))
    ).thenReturn(participantBuiltinTopicData);

    // setup mock for publication topic data
    PublicationBuiltinTopicData data = mock(PublicationBuiltinTopicData.class);
    BuiltinTopicKey_t key = PowerMockito.mock(BuiltinTopicKey_t.class);
    Whitebox.setInternalState(data, "participant_key", key);

    // setup mock for instance handle
    InstanceHandle_t instanceHandle = mock(InstanceHandle_t.class);

    // call ignore publication
    assertTrue(filter.ignorePublication(domainParticipant, instanceHandle, data));
  }

  @Test
  public void testIgnorePublicationFalse() throws Exception {
    // setup mock for participant data
    ParticipantBuiltinTopicData participantBuiltinTopicData = mock(ParticipantBuiltinTopicData.class);
    {
      ServiceQosPolicy serviceQosPolicy = PowerMockito.mock(ServiceQosPolicy.class);
      serviceQosPolicy.kind = ServiceQosPolicyKind.NO_SERVICE_QOS;
      Whitebox.setInternalState(participantBuiltinTopicData, "service", serviceQosPolicy);
    }
    PowerMockito.whenNew(ParticipantBuiltinTopicData.class).withAnyArguments().thenReturn(participantBuiltinTopicData);

    // setup mock for topic helper
    PowerMockito.mockStatic(BuiltinTopicHelper.class);
    when(BuiltinTopicHelper.getParticipantBuiltinTopicData(
      any(DomainParticipant.class), any(BuiltinTopicKey_t.class))
    ).thenReturn(participantBuiltinTopicData);

    // setup mock for publication topic data
    PublicationBuiltinTopicData data = mock(PublicationBuiltinTopicData.class);
    BuiltinTopicKey_t key = PowerMockito.mock(BuiltinTopicKey_t.class);
    Whitebox.setInternalState(data, "participant_key", key);

    // setup mock for instance handle
    InstanceHandle_t instanceHandle = mock(InstanceHandle_t.class);

    // call ignore publication
    assertFalse(filter.ignorePublication(domainParticipant, instanceHandle, data));
  }

  @Test
  public void testIgnoreSubscriptionTrue() throws Exception {
    // setup mock for participant data
    ParticipantBuiltinTopicData participantBuiltinTopicData = mock(ParticipantBuiltinTopicData.class);
    {
      ServiceQosPolicy serviceQosPolicy = PowerMockito.mock(ServiceQosPolicy.class);
      serviceQosPolicy.kind = ServiceQosPolicyKind.ROUTING_SERVICE_QOS;
      Whitebox.setInternalState(participantBuiltinTopicData, "service", serviceQosPolicy);
    }
    PowerMockito.whenNew(ParticipantBuiltinTopicData.class).withAnyArguments().thenReturn(participantBuiltinTopicData);

    // setup mock for topic helper
    PowerMockito.mockStatic(BuiltinTopicHelper.class);
    when(BuiltinTopicHelper.getParticipantBuiltinTopicData(
      any(DomainParticipant.class), any(BuiltinTopicKey_t.class))
    ).thenReturn(participantBuiltinTopicData);

    // setup mock for subscription topic data
    SubscriptionBuiltinTopicData data = mock(SubscriptionBuiltinTopicData.class);
    BuiltinTopicKey_t key = PowerMockito.mock(BuiltinTopicKey_t.class);
    Whitebox.setInternalState(data, "participant_key", key);

    // setup mock for instance handle
    InstanceHandle_t instanceHandle = mock(InstanceHandle_t.class);

    // call ignore subscription
    assertTrue(filter.ignoreSubscription(domainParticipant, instanceHandle, data));
  }

  @Test
  public void testIgnoreSubscriptionFalse() throws Exception {
    // setup mock for participant data
    ParticipantBuiltinTopicData participantBuiltinTopicData = mock(ParticipantBuiltinTopicData.class);
    {
      ServiceQosPolicy serviceQosPolicy = PowerMockito.mock(ServiceQosPolicy.class);
      serviceQosPolicy.kind = ServiceQosPolicyKind.NO_SERVICE_QOS;
      Whitebox.setInternalState(participantBuiltinTopicData, "service", serviceQosPolicy);
    }
    PowerMockito.whenNew(ParticipantBuiltinTopicData.class).withAnyArguments().thenReturn(participantBuiltinTopicData);

    // setup mock for topic helper
    PowerMockito.mockStatic(BuiltinTopicHelper.class);
    when(BuiltinTopicHelper.getParticipantBuiltinTopicData(
      any(DomainParticipant.class), any(BuiltinTopicKey_t.class))
    ).thenReturn(participantBuiltinTopicData);

    // setup mock for subscription topic data
    SubscriptionBuiltinTopicData data = mock(SubscriptionBuiltinTopicData.class);
    BuiltinTopicKey_t key = PowerMockito.mock(BuiltinTopicKey_t.class);
    Whitebox.setInternalState(data, "participant_key", key);

    // setup mock for instance handle
    InstanceHandle_t instanceHandle = mock(InstanceHandle_t.class);

    // call ignore subscription
    assertFalse(filter.ignoreSubscription(domainParticipant, instanceHandle, data));
  }

  @Test
  public void testIgnorePartition() {
    assertFalse(filter.ignorePartition("", ""));
    assertFalse(filter.ignorePartition("A", ""));
    assertFalse(filter.ignorePartition("", "A"));
  }
}
