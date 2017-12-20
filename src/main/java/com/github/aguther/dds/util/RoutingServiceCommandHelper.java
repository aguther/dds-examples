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

package com.github.aguther.dds.util;

import com.rti.connext.infrastructure.Sample;
import com.rti.connext.requestreply.Requester;
import com.rti.connext.requestreply.RequesterParams;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.domain.builtin.ParticipantBuiltinTopicData;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.InstanceHandleSeq;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.ServiceQosPolicyKind;
import idl.RTI.RoutingService.Administration.COMMAND_REQUEST_TOPIC_NAME;
import idl.RTI.RoutingService.Administration.COMMAND_RESPONSE_TOPIC_NAME;
import idl.RTI.RoutingService.Administration.CommandRequest;
import idl.RTI.RoutingService.Administration.CommandRequestTypeSupport;
import idl.RTI.RoutingService.Administration.CommandResponse;
import idl.RTI.RoutingService.Administration.CommandResponseTypeSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutingServiceCommandHelper {

  private static final Logger log;

  static {
    log = LoggerFactory.getLogger(RoutingServiceCommandHelper.class);
  }

  private final Requester<CommandRequest, CommandResponse> requester;

  private int idHost;
  private int idApplication;
  private int idInvocationCounter;

  public RoutingServiceCommandHelper(
      DomainParticipant domainParticipant
  ) {
    // check input parameters
    if (domainParticipant == null) {
      throw new IllegalArgumentException("DomainParticipant must not be null.");
    }

    // get host and app id from wire protocol of domain participant
    DomainParticipantQos domainParticipantQos = new DomainParticipantQos();
    domainParticipant.get_qos(domainParticipantQos);
    idHost = domainParticipantQos.wire_protocol.rtps_host_id;
    idApplication = domainParticipantQos.wire_protocol.rtps_app_id;

    // set invocation counter
    idInvocationCounter = 0;

    // create parameters for requester
    RequesterParams requesterParams = new RequesterParams(
        domainParticipant,
        CommandRequestTypeSupport.get_instance(),
        CommandResponseTypeSupport.get_instance()
    );
    requesterParams.setRequestTopicName(COMMAND_REQUEST_TOPIC_NAME.VALUE);
    requesterParams.setReplyTopicName(COMMAND_RESPONSE_TOPIC_NAME.VALUE);

    // create requester for routing service administration
    requester = new Requester<>(requesterParams);
  }

  public boolean waitForRoutingService(
      String targetRouter,
      Duration_t timeOut
  ) {
    return waitForRoutingService(
        targetRouter,
        timeOut,
        new Duration_t(0, 250000000)
    );
  }

  public boolean waitForRoutingService(
      String targetRouter,
      Duration_t timeOut,
      Duration_t sleepTime
  ) {
    // create participant name for target router according RTI conventions
    String participantNameTargetRouter = String.format("RTI Routing Service: %s", targetRouter);

    // calculate sleep time
    long sleepTimeMillis = sleepTime.sec * 1000L + sleepTime.nanosec / 1000000L;

    try {
      // variables to store the data
      InstanceHandleSeq instanceHandles = new InstanceHandleSeq();
      ParticipantBuiltinTopicData participantData = new ParticipantBuiltinTopicData();

      // store start time
      long startTime = System.currentTimeMillis();
      // determine end time
      long endTime = startTime + timeOut.sec * 1000L + timeOut.nanosec / 1000000L;

      while (System.currentTimeMillis() < endTime) {
        // get matched subscriptions
        requester.getRequestDataWriter().get_matched_subscriptions(instanceHandles);

        // iterate over instance handles
        for (Object participantHandle : instanceHandles) {
          // get participant data of subscription
          requester.getRequestDataWriter().get_matched_subscription_participant_data(
              participantData,
              (InstanceHandle_t) participantHandle
          );

          // check if related participant is from routing service
          if (participantData.service.kind == ServiceQosPolicyKind.ROUTING_SERVICE_QOS
              && participantData.participant_name.name.equals(participantNameTargetRouter)) {
            break;
          }
        }

        // sleep for some time
        Thread.sleep(sleepTimeMillis);
      }

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return true;
  }

  public CommandRequest createCommandRequest() {
    return new CommandRequest();
  }

  public CommandResponse sendRequest(
      CommandRequest commandRequest,
      Duration_t timeOut
  ) {
    // set identification
    commandRequest.id.host = idHost;
    commandRequest.id.app = idApplication;
    commandRequest.id.invocation = ++idInvocationCounter;

    // send request
    requester.sendRequest(commandRequest);

    // create reply
    Sample<CommandResponse> reply = requester.createReplySample();

    // wait for reply
    boolean replyReceived = requester.receiveReply(reply, timeOut);

    // return result
    return replyReceived ? reply.getData() : null;
  }
}
