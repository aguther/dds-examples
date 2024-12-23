/*
 * MIT License
 *
 * Copyright (c) 2019 Andreas Guther
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

package io.github.aguther.dds.routing.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.rti.connext.infrastructure.Sample;
import com.rti.connext.requestreply.Requester;
import com.rti.connext.requestreply.RequesterParams;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.domain.builtin.ParticipantBuiltinTopicData;
import com.rti.dds.infrastructure.InstanceHandleSeq;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.ServiceQosPolicyKind;
import idl.RTI.Service.Admin.COMMAND_REPLY_TOPIC_NAME;
import idl.RTI.Service.Admin.COMMAND_REQUEST_TOPIC_NAME;
import idl.RTI.Service.Admin.CommandReply;
import idl.RTI.Service.Admin.CommandReplyTypeSupport;
import idl.RTI.Service.Admin.CommandRequest;
import idl.RTI.Service.Admin.CommandRequestTypeSupport;
import io.github.aguther.dds.util.DurationFactory;
import java.io.Closeable;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class provides helpers to easily interact with a routing service using the topics defined by RTI.
 */
public class RoutingServiceCommandInterface implements Closeable {

  private static final Logger LOGGER = LogManager.getLogger(RoutingServiceCommandInterface.class);

  private final Requester<CommandRequest, CommandReply> requester;

  /**
   * Instantiates a new routing service command helper.
   *
   * @param domainParticipant domain participant to send and receive commands
   */
  public RoutingServiceCommandInterface(
    final DomainParticipant domainParticipant
  ) {
    // check input parameters
    checkNotNull(domainParticipant, "DomainParticipant must not be null");

    // get host and app id from wire protocol of domain participant
    DomainParticipantQos domainParticipantQos = new DomainParticipantQos();
    domainParticipant.get_qos(domainParticipantQos);

    // create parameters for requester
    RequesterParams requesterParams = new RequesterParams(
      domainParticipant,
      CommandRequestTypeSupport.get_instance(),
      CommandReplyTypeSupport.get_instance()
    );
    requesterParams.setRequestTopicName(COMMAND_REQUEST_TOPIC_NAME.VALUE);
    requesterParams.setReplyTopicName(COMMAND_REPLY_TOPIC_NAME.VALUE);

    // create requester for routing service administration
    requester = new Requester<>(requesterParams);
  }


  @Override
  public void close() {
    if (requester != null) {
      requester.close();
    }
  }

  /**
   * Waits for a specific routing service instance to be discovered.
   *
   * @param targetRouter target routing service
   * @param timeOut      timeout
   * @param timeOutUnit  time unit of timeout
   * @return true if target routing service was discovered, false if not within timeout
   */
  public boolean waitForDiscovery(
    final String targetRouter,
    final long timeOut,
    final TimeUnit timeOutUnit
  ) {
    return waitForDiscovery(
      targetRouter,
      timeOut,
      timeOutUnit,
      250L,
      TimeUnit.MILLISECONDS
    );
  }

  /**
   * Waits for a specific routing service instance to be discovered.
   *
   * @param targetRouter  target routing service
   * @param timeOut       timeout
   * @param timeOutUnit   time unit of timeout
   * @param sleepTime     time to sleep between checks
   * @param sleepTimeUnit time unit of time to sleep
   * @return true if target routing service was discovered, false if not within timeout
   */
  public boolean waitForDiscovery(
    final String targetRouter,
    final long timeOut,
    final TimeUnit timeOutUnit,
    final long sleepTime,
    final TimeUnit sleepTimeUnit
  ) {
    // create participant name for target router according RTI conventions
    String participantNameTargetRouter = String.format("RTI Routing Service: %s", targetRouter);

    try {
      // variables to store the data
      InstanceHandleSeq instanceHandles = new InstanceHandleSeq();
      ParticipantBuiltinTopicData participantData = new ParticipantBuiltinTopicData();

      // store start time
      long startTime = System.currentTimeMillis();
      // determine end time
      long endTime = startTime + timeOutUnit.toMillis(timeOut);

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
            // we discovered the target routing service
            return true;
          }
        }

        // sleep for some time
        sleepTimeUnit.sleep(sleepTime);
      }

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // we did not discover the target routing service
    return false;
  }

  /**
   * Creates a new instance of the command request.
   *
   * @return new instance
   */
  public CommandRequest createCommandRequest() {
    return new CommandRequest();
  }

  /**
   * Sends a request to a target routing service and returns the response when received within timeout.
   *
   * @param commandRequest request to send
   * @param timeOut        timeout
   * @param timeUnit       time unit of timeout
   * @return response if received within timeout, otherwise null
   */
  public CommandReply sendRequest(
    final CommandRequest commandRequest,
    final long timeOut,
    final TimeUnit timeUnit
  ) {
    // logging
    logCommandRequest(commandRequest);

    // send request
    requester.sendRequest(commandRequest);

    // create reply
    Sample<CommandReply> reply = requester.createReplySample();

    // wait for reply
    boolean replyReceived = requester.receiveReply(
      reply,
      DurationFactory.from(timeOut, timeUnit)
    );

    // logging
    logCommandResponse(reply, replyReceived);

    // return result
    return replyReceived ? reply.getData() : null;
  }

  /**
   * Logs some important things of a command request.
   *
   * @param commandRequest request that should be logged
   */
  private void logCommandRequest(
    final CommandRequest commandRequest
  ) {
    // trace logs
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace(
        "CommandRequest.string_body.length()='{}'",
        commandRequest.string_body.length()
      );
      LOGGER.trace(
        "CommandRequest {}",
        commandRequest.toString().replace("\n", "").replaceAll("[ ]{2,}", " ")
      );
    }
  }

  /**
   * Logs some important things of a command response.
   *
   * @param reply         response that should be logged
   * @param replyReceived true if response is valid, otherwise false
   */
  private void logCommandResponse(
    final Sample<CommandReply> reply,
    final boolean replyReceived
  ) {
    // trace logs
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace(
        "CommandReply {}",
        replyReceived ?
          reply.getData().toString().replace(
            "\n", "").replaceAll("[ ]{2,}", " ")
          : "<no response received>"
      );
    }
  }
}
