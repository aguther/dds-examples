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

import com.rti.connext.infrastructure.Sample;
import com.rti.connext.requestreply.Requester;
import com.rti.connext.requestreply.RequesterParams;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.InstanceHandleSeq;
import com.rti.dds.publication.DataWriterQos;
import idl.RTI.RoutingService.Administration.COMMAND_REQUEST_TOPIC_NAME;
import idl.RTI.RoutingService.Administration.COMMAND_RESPONSE_TOPIC_NAME;
import idl.RTI.RoutingService.Administration.CommandRequest;
import idl.RTI.RoutingService.Administration.CommandRequestTypeSupport;
import idl.RTI.RoutingService.Administration.CommandResponse;
import idl.RTI.RoutingService.Administration.CommandResponseTypeSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutingServiceCommander {

  private static final Logger log;

  static {
    log = LoggerFactory.getLogger(RoutingServiceCommander.class);
  }

  private final Requester<CommandRequest, CommandResponse> requester;

  private int idHost;
  private int idApplication;
  private int idInvocationCounter;

  public RoutingServiceCommander(
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

  public boolean waitForMatchedSubscriptions() {
    try {
      // holds instance handles of matched subscriptions
      InstanceHandleSeq handles = new InstanceHandleSeq();

      do {
        // get matched subscriptions
        requester.getRequestDataWriter().get_matched_subscriptions(handles);

        // wait some time
        Thread.sleep(200);

      } while (handles.isEmpty());

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
