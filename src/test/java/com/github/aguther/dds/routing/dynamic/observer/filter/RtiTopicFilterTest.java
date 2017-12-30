package com.github.aguther.dds.routing.dynamic.observer.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.github.aguther.dds.util.BuiltinTopicHelper;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.builtin.ParticipantBuiltinTopicData;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.ServiceQosPolicy;
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
    RtiTopicFilter.class,
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
public class RtiTopicFilterTest {

  private DomainParticipant domainParticipant;
  private RtiTopicFilter filter;

  @Before
  public void setUp() {
    domainParticipant = mock(DomainParticipant.class);
    filter = new RtiTopicFilter();
  }

  @After
  public void tearDown() {
    domainParticipant = null;
    filter = null;
  }

  @Test
  public void testIgnorePublicationTrue() throws Exception {
    // setup mock for publication topic data
    PublicationBuiltinTopicData data = mock(PublicationBuiltinTopicData.class);
    BuiltinTopicKey_t key = PowerMockito.mock(BuiltinTopicKey_t.class);
    Whitebox.setInternalState(data, "participant_key", key);
    Whitebox.setInternalState(data, "topic_name", "rti");

    // setup mock for instance handle
    InstanceHandle_t instanceHandle = mock(InstanceHandle_t.class);

    // call ignore publication
    assertTrue(filter.ignorePublication(domainParticipant, instanceHandle, data));
  }

  @Test
  public void testIgnorePublicationFalse() throws Exception {
    // setup mock for publication topic data
    PublicationBuiltinTopicData data = mock(PublicationBuiltinTopicData.class);
    BuiltinTopicKey_t key = PowerMockito.mock(BuiltinTopicKey_t.class);
    Whitebox.setInternalState(data, "participant_key", key);
    Whitebox.setInternalState(data, "topic_name", "topic");

    // setup mock for instance handle
    InstanceHandle_t instanceHandle = mock(InstanceHandle_t.class);

    // call ignore publication
    assertFalse(filter.ignorePublication(domainParticipant, instanceHandle, data));
  }

  @Test
  public void testIgnoreSubscriptionTrue() throws Exception {
    // setup mock for subscription topic data
    SubscriptionBuiltinTopicData data = mock(SubscriptionBuiltinTopicData.class);
    BuiltinTopicKey_t key = PowerMockito.mock(BuiltinTopicKey_t.class);
    Whitebox.setInternalState(data, "participant_key", key);
    Whitebox.setInternalState(data, "topic_name", "rti");

    // setup mock for instance handle
    InstanceHandle_t instanceHandle = mock(InstanceHandle_t.class);

    // call ignore subscription
    assertTrue(filter.ignoreSubscription(domainParticipant, instanceHandle, data));
  }

  @Test
  public void testIgnoreSubscriptionFalse() throws Exception {
    // setup mock for subscription topic data
    SubscriptionBuiltinTopicData data = mock(SubscriptionBuiltinTopicData.class);
    BuiltinTopicKey_t key = PowerMockito.mock(BuiltinTopicKey_t.class);
    Whitebox.setInternalState(data, "participant_key", key);
    Whitebox.setInternalState(data, "topic_name", "topic");

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