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

package io.github.aguther.dds.util;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.builtin.ParticipantBuiltinTopicData;
import com.rti.dds.infrastructure.InstanceHandleSeq;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import com.rti.dds.topic.BuiltinTopicKey_t;
import java.nio.ByteBuffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    BuiltinTopicHelper.class,
    InstanceHandleSeq.class,
    ParticipantBuiltinTopicData.class,
    PublicationBuiltinTopicData.class,
    SubscriptionBuiltinTopicData.class,
})
@SuppressStaticInitializationFor({
    "com.rti.dds.topic.AbstractBuiltinTopicData",
})
public class BuiltinTopicHelperTest {

  private InstanceHandle_t instanceHandleA;
  private BuiltinTopicKey_t builtinTopicKeyA;

  private InstanceHandle_t instanceHandleB;
  private BuiltinTopicKey_t builtinTopicKeyB;

  private InstanceHandle_t instanceHandleC;
  private BuiltinTopicKey_t builtinTopicKeyC;

  private DomainParticipant domainParticipant;

  @Before
  public void setUp() {
    instanceHandleA = new InstanceHandle_t(ByteBuffer.allocate(4).putInt(1).array());
    builtinTopicKeyA = new BuiltinTopicKey_t();
    Whitebox.setInternalState(builtinTopicKeyA, "value", new int[]{0, 0, 0, 1});

    instanceHandleB = new InstanceHandle_t(ByteBuffer.allocate(4).putInt(2).array());
    builtinTopicKeyB = new BuiltinTopicKey_t();
    Whitebox.setInternalState(builtinTopicKeyB, "value", new int[]{0, 0, 0, 2});

    instanceHandleC = new InstanceHandle_t(ByteBuffer.allocate(4).putInt(3).array());
    builtinTopicKeyC = new BuiltinTopicKey_t();
    Whitebox.setInternalState(builtinTopicKeyC, "value", new int[]{0, 0, 0, 3});

    domainParticipant = mock(DomainParticipant.class);

    doAnswer(invocation -> {
      InstanceHandleSeq seq = invocation.getArgument(0);
      seq.add(instanceHandleA);
      seq.add(instanceHandleB);
      return null;
    }).when(domainParticipant).get_discovered_participants(any(InstanceHandleSeq.class));

    doAnswer(invocation -> {
      ParticipantBuiltinTopicData data = invocation.getArgument(0);
      Whitebox.setInternalState(data, "key", builtinTopicKeyA);
      data.participant_name.name = "A";
      return null;
    }).when(domainParticipant).get_discovered_participant_data(
        any(ParticipantBuiltinTopicData.class),
        eq(instanceHandleA)
    );

    doAnswer(invocation -> {
      ParticipantBuiltinTopicData data = invocation.getArgument(0);
      Whitebox.setInternalState(data, "key", builtinTopicKeyB);
      data.participant_name.name = "B";
      return null;
    }).when(domainParticipant).get_discovered_participant_data(
        any(ParticipantBuiltinTopicData.class),
        eq(instanceHandleB)
    );
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testFromPublicationDataA() {
    PublicationBuiltinTopicData publicationBuiltinTopicData = new PublicationBuiltinTopicData();
    Whitebox.setInternalState(publicationBuiltinTopicData, "participant_key", builtinTopicKeyA);

    ParticipantBuiltinTopicData result = BuiltinTopicHelper.getParticipantBuiltinTopicData(
        domainParticipant, publicationBuiltinTopicData);

    assertTrue(("A").equals(result.participant_name.name));
  }

  @Test
  public void testFromPublicationDataB() {
    PublicationBuiltinTopicData publicationBuiltinTopicData = new PublicationBuiltinTopicData();
    Whitebox.setInternalState(publicationBuiltinTopicData, "participant_key", builtinTopicKeyB);

    ParticipantBuiltinTopicData result = BuiltinTopicHelper.getParticipantBuiltinTopicData(
        domainParticipant, publicationBuiltinTopicData);

    assertTrue(("B").equals(result.participant_name.name));
  }

  @Test
  public void testFromPublicationDataNotFound() {
    PublicationBuiltinTopicData publicationBuiltinTopicData = new PublicationBuiltinTopicData();
    Whitebox.setInternalState(publicationBuiltinTopicData, "participant_key", builtinTopicKeyC);

    ParticipantBuiltinTopicData result = BuiltinTopicHelper.getParticipantBuiltinTopicData(
        domainParticipant, publicationBuiltinTopicData);

    assertNull(result);
  }

  @Test
  public void testFromSubscriptionDataA() {
    SubscriptionBuiltinTopicData subscriptionBuiltinTopicData = new SubscriptionBuiltinTopicData();
    Whitebox.setInternalState(subscriptionBuiltinTopicData, "participant_key", builtinTopicKeyA);

    ParticipantBuiltinTopicData result = BuiltinTopicHelper.getParticipantBuiltinTopicData(
        domainParticipant, subscriptionBuiltinTopicData);

    assertTrue(("A").equals(result.participant_name.name));
  }

  @Test
  public void testFromSubscriptionDataB() {
    SubscriptionBuiltinTopicData subscriptionBuiltinTopicData = new SubscriptionBuiltinTopicData();
    Whitebox.setInternalState(subscriptionBuiltinTopicData, "participant_key", builtinTopicKeyB);

    ParticipantBuiltinTopicData result = BuiltinTopicHelper.getParticipantBuiltinTopicData(
        domainParticipant, subscriptionBuiltinTopicData);

    assertTrue(("B").equals(result.participant_name.name));
  }

  @Test
  public void testFromSubscriptionDataNotFound() {
    SubscriptionBuiltinTopicData subscriptionBuiltinTopicData = new SubscriptionBuiltinTopicData();
    Whitebox.setInternalState(subscriptionBuiltinTopicData, "participant_key", builtinTopicKeyC);

    ParticipantBuiltinTopicData result = BuiltinTopicHelper.getParticipantBuiltinTopicData(
        domainParticipant, subscriptionBuiltinTopicData);

    assertNull(result);
  }
}
