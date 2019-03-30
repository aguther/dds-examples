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

package com.github.aguther.dds.routing.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rti.connext.infrastructure.Sample;
import com.rti.connext.requestreply.Requester;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.domain.builtin.ParticipantBuiltinTopicData;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.EntityNameQosPolicy;
import com.rti.dds.infrastructure.InstanceHandleSeq;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.ServiceQosPolicy;
import com.rti.dds.infrastructure.ServiceQosPolicyKind;
import com.rti.dds.infrastructure.WireProtocolQosPolicy;
import com.rti.dds.publication.DataWriter;
import idl.RTI.Service.Admin.CommandReply;
import idl.RTI.Service.Admin.CommandReplyTypeSupport;
import idl.RTI.Service.Admin.CommandRequest;
import idl.RTI.Service.Admin.CommandRequestTypeSupport;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
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
    RoutingServiceCommandInterface.class,
    DomainParticipant.class,
    DomainParticipantQos.class,
    EntityNameQosPolicy.class,
    ServiceQosPolicy.class,
    WireProtocolQosPolicy.class,
    ParticipantBuiltinTopicData.class,
    CommandRequestTypeSupport.class,
    CommandReplyTypeSupport.class,
    Requester.class
})
@SuppressStaticInitializationFor({
    "com.rti.dds.domain.DomainParticipantFactory",
    "com.rti.dds.domain.DomainParticipant",
    "com.rti.dds.domain.DomainParticipantQos",
    "com.rti.dds.domain.builtin.ParticipantBuiltinTopicDataTypeSupport",
    "com.rti.dds.topic.TypeSupportImpl",
    "com.rti.dds.topic.builtin.ServiceRequestTypeSupport",
    "com.rti.dds.topic.builtin.TopicBuiltinTopicDataTypeSupport",
    "com.rti.dds.publication.builtin.PublicationBuiltinTopicDataTypeSupport",
    "com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicDataTypeSupport",
    "idl.RTI.RoutingService.Administration.CommandRequestTypeSupport",
    "idl.RTI.RoutingService.Administration.CommandResponseTypeSupport"
})
public class RoutingServiceCommandInterfaceTest {

  private DomainParticipant domainParticipant;
  private DomainParticipantQos domainParticipantQos;

  private CommandRequestTypeSupport commandRequestTypeSupport;
  private CommandReplyTypeSupport commandResponseTypeSupport;

  private Requester<CommandRequest, CommandReply> requester;

  private RoutingServiceCommandInterface commandInterface;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() throws Exception {
    domainParticipant = mock(DomainParticipant.class);

    domainParticipantQos = mock(DomainParticipantQos.class);
    {
      WireProtocolQosPolicy wireProtocolQosPolicy = PowerMockito.mock(WireProtocolQosPolicy.class);
      Whitebox.setInternalState(domainParticipantQos, "wire_protocol", wireProtocolQosPolicy);
    }
    PowerMockito.whenNew(DomainParticipantQos.class).withAnyArguments().thenReturn(domainParticipantQos);

    commandRequestTypeSupport = mock(CommandRequestTypeSupport.class);
    Whitebox.setInternalState(CommandRequestTypeSupport.class, "_singleton", commandRequestTypeSupport);

    commandResponseTypeSupport = mock(CommandReplyTypeSupport.class);
    Whitebox.setInternalState(CommandReplyTypeSupport.class, "_singleton", commandResponseTypeSupport);

    requester = mock(Requester.class);
    {
      Sample responseSample = mock(Sample.class);
      when(responseSample.getData()).thenReturn(new CommandReply());
      when(requester.createReplySample()).thenReturn(responseSample);
    }
    PowerMockito.whenNew(Requester.class).withAnyArguments().thenReturn(requester);

    commandInterface = new RoutingServiceCommandInterface(domainParticipant);
  }

  @After
  public void tearDown() {

  }

  @Test
  public void testWaitForRoutingServiceFound() throws Exception {
    // set target router name
    String targetRouter = "Test";

    // instance handle for identification
    InstanceHandle_t instanceHandle = new InstanceHandle_t(ByteBuffer.allocate(4).putInt(1).array());

    // setup mock for data writer
    DataWriter dataWriter = mock(DataWriter.class);
    when(requester.getRequestDataWriter()).thenReturn(dataWriter);

    // setup mock for participant data
    ParticipantBuiltinTopicData participantBuiltinTopicData = mock(ParticipantBuiltinTopicData.class);
    {
      ServiceQosPolicy serviceQosPolicy = PowerMockito.mock(ServiceQosPolicy.class);
      serviceQosPolicy.kind = ServiceQosPolicyKind.ROUTING_SERVICE_QOS;
      Whitebox.setInternalState(participantBuiltinTopicData, "service", serviceQosPolicy);

      EntityNameQosPolicy entityNameQosPolicy = PowerMockito.mock(EntityNameQosPolicy.class);
      entityNameQosPolicy.name = String.format("RTI Routing Service: %s", targetRouter);
      Whitebox.setInternalState(participantBuiltinTopicData, "participant_name", entityNameQosPolicy);
    }
    PowerMockito.whenNew(ParticipantBuiltinTopicData.class).withAnyArguments().thenReturn(participantBuiltinTopicData);

    // prepare answer to get subscription data
    doAnswer(
        invocation -> {
          InstanceHandleSeq seq = invocation.getArgument(0);
          seq.add(instanceHandle);
          return null;
        }
    ).when(dataWriter).get_matched_subscriptions(any(InstanceHandleSeq.class));

    // assert that target router is found
    assertTrue(commandInterface.waitForDiscovery(targetRouter, 100, TimeUnit.MILLISECONDS));
  }

  @Test
  public void testWaitForRoutingServiceNotFound() throws Exception {
    // set target router name
    String targetRouter = "Test";

    // instance handle for identification
    InstanceHandle_t instanceHandle = new InstanceHandle_t(ByteBuffer.allocate(4).putInt(1).array());

    // setup mock for data writer
    DataWriter dataWriter = mock(DataWriter.class);
    when(requester.getRequestDataWriter()).thenReturn(dataWriter);

    // setup mock for participant data
    ParticipantBuiltinTopicData participantBuiltinTopicData = mock(ParticipantBuiltinTopicData.class);
    {
      ServiceQosPolicy serviceQosPolicy = PowerMockito.mock(ServiceQosPolicy.class);
      serviceQosPolicy.kind = ServiceQosPolicyKind.NO_SERVICE_QOS;
      Whitebox.setInternalState(participantBuiltinTopicData, "service", serviceQosPolicy);

      EntityNameQosPolicy entityNameQosPolicy = PowerMockito.mock(EntityNameQosPolicy.class);
      entityNameQosPolicy.name = "Some other participant";
      Whitebox.setInternalState(participantBuiltinTopicData, "participant_name", entityNameQosPolicy);
    }
    PowerMockito.whenNew(ParticipantBuiltinTopicData.class).withAnyArguments().thenReturn(participantBuiltinTopicData);

    // prepare answer to get subscription data
    doAnswer(
        invocation -> {
          InstanceHandleSeq seq = invocation.getArgument(0);
          seq.add(instanceHandle);
          return null;
        }
    ).when(dataWriter).get_matched_subscriptions(any(InstanceHandleSeq.class));

    // assert that target router is found
    assertFalse(commandInterface.waitForDiscovery(targetRouter, 100, TimeUnit.MILLISECONDS));
  }

  @Test
  public void testWaitForRoutingServiceThreadInterrupted() throws Exception {
    // set target router name
    String targetRouter = "Test";

    // instance handle for identification
    InstanceHandle_t instanceHandle = new InstanceHandle_t(ByteBuffer.allocate(4).putInt(1).array());

    // setup mock for data writer
    DataWriter dataWriter = mock(DataWriter.class);
    when(requester.getRequestDataWriter()).thenReturn(dataWriter);

    // setup mock for participant data
    ParticipantBuiltinTopicData participantBuiltinTopicData = mock(ParticipantBuiltinTopicData.class);
    PowerMockito.whenNew(ParticipantBuiltinTopicData.class).withAnyArguments().thenReturn(participantBuiltinTopicData);

    // setup mock for time unit
    TimeUnit timeUnit = mock(TimeUnit.class);
    doThrow(new InterruptedException()).when(timeUnit).sleep(anyLong());

    // wait for discovery and interrupt thread
    commandInterface.waitForDiscovery(
        targetRouter,
        100,
        TimeUnit.MILLISECONDS,
        100,
        timeUnit
    );

    // assert that thread was interrupted
    assertTrue(Thread.currentThread().isInterrupted());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSendRequest() {
    CommandRequest commandRequest = commandInterface.createCommandRequest();

    when(requester.receiveReply(any(Sample.class), any(Duration_t.class))).thenReturn(true);
    CommandReply response = commandInterface.sendRequest(commandRequest, 1, TimeUnit.SECONDS);

    assertNotNull(response);

    verify(requester, times(1)).sendRequest(commandRequest);
    verify(requester, times(1)).createReplySample();
    verify(requester, times(1)).receiveReply(any(Sample.class), any(Duration_t.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSendRequestFailed() {
    CommandRequest commandRequest = commandInterface.createCommandRequest();

    when(requester.receiveReply(any(Sample.class), any(Duration_t.class))).thenReturn(false);
    CommandReply response = commandInterface.sendRequest(commandRequest, 1, TimeUnit.SECONDS);

    assertNull(response);

    verify(requester, times(1)).sendRequest(commandRequest);
    verify(requester, times(1)).createReplySample();
    verify(requester, times(1)).receiveReply(any(Sample.class), any(Duration_t.class));
  }

  @Test
  public void testClose() {
    commandInterface.close();
    verify(requester, times(1)).close();
  }
}
