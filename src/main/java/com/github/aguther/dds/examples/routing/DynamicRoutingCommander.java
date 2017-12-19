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

package com.github.aguther.dds.examples.routing;

import com.github.aguther.dds.examples.routing.dynamic.command.RoutingServiceCommander;
import com.github.aguther.dds.examples.routing.dynamic.observer.DynamicPartitionObserverListener;
import com.github.aguther.dds.examples.routing.dynamic.observer.Session;
import com.github.aguther.dds.examples.routing.dynamic.observer.TopicRoute;
import com.github.aguther.dds.examples.routing.dynamic.observer.TopicRoute.Direction;
import com.rti.dds.infrastructure.Duration_t;
import idl.RTI.RoutingService.Administration.CommandKind;
import idl.RTI.RoutingService.Administration.CommandRequest;
import idl.RTI.RoutingService.Administration.CommandResponse;
import idl.RTI.RoutingService.Administration.CommandResponseKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicRoutingCommander implements DynamicPartitionObserverListener {

  private static final Duration_t REQUEST_TIMEOUT;

  private static final Logger log;

  static {
    REQUEST_TIMEOUT = new Duration_t(10, 0);
    log = LoggerFactory.getLogger(DynamicRoutingCommander.class);
  }

  private final RoutingServiceCommander routingServiceCommander;
  private final String targetRouter;

  public DynamicRoutingCommander(
      RoutingServiceCommander routingServiceCommander,
      String targetRouter
  ) {
    this.routingServiceCommander = routingServiceCommander;
    this.targetRouter = targetRouter;
  }

  @Override
  public void createSession(
      Session session
  ) {
    log.info(
        "Create session: topic='{}', partition='{}'",
        session.getTopic(),
        session.getPartition()
    );

    // create request
    CommandRequest commandRequest = routingServiceCommander.createCommandRequest();
    commandRequest.target_router = targetRouter;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    commandRequest.command.entity_desc.name = "Default";
    commandRequest.command.entity_desc.xml_url.content = String
        .format(
            "str://\"<session name=\"%1$s\"><publisher_qos><partition><name><element>%2$s</element></name></partition></publisher_qos><subscriber_qos><partition><name><element>%2$s</element></name></partition></subscriber_qos></session>\"",
            String.format("%s(%s)", session.getTopic(), session.getPartition()),
            session.getPartition());
    commandRequest.command.entity_desc.xml_url.is_final = true;

    // send request
    CommandResponse reply = routingServiceCommander.sendRequest(
        commandRequest, REQUEST_TIMEOUT);

    // reply received?
    if (reply == null) {
      log.error(
          "No reply received when creating session for topic '{};{}'",
          session.getTopic(),
          session.getPartition()
      );
      return;
    }
    // reply success or failed?
    if (reply.kind == CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK) {
      log.info(
          "Created session for topic='{}', partition='{}'",
          session.getTopic(),
          session.getPartition()
      );
    } else {
      log.error(
          "Failed to create session for topic='{}', partition='{}', reason: {}, {}",
          session.getTopic(),
          session.getPartition(),
          reply.kind,
          reply.message
      );
    }
  }

  @Override
  public void deleteSession(
      Session session
  ) {
    log.info(
        "Delete session: topic='{}', partition='{}'",
        session.getTopic(),
        session.getPartition()
    );

    // create request
    CommandRequest commandRequest = routingServiceCommander.createCommandRequest();
    commandRequest.target_router = targetRouter;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    commandRequest.command.entity_name = String.format(
        "Default::%s", String.format("%s(%s)", session.getTopic(), session.getPartition()));

    // send request
    CommandResponse reply = routingServiceCommander.sendRequest(
        commandRequest, REQUEST_TIMEOUT);

    // reply received?
    if (reply == null) {
      log.error(
          "No reply received when deleting session for topic='{}', partition='{}'",
          session.getTopic(),
          session.getPartition()
      );
      return;
    }
    // reply success or failed?
    if (reply.kind == CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK) {
      log.info(
          "Deleted session for topic='{}', partition='{}'",
          session.getTopic(),
          session.getPartition()
      );
    } else {
      log.error(
          "Failed to delete session for topic='{}', partition='{}', reason: {}, {}",
          session.getTopic(),
          session.getPartition(),
          reply.kind,
          reply.message
      );
    }
  }

  @Override
  public void createTopicRoute(
      Session session,
      TopicRoute topicRoute
  ) {
    log.info(
        "Create route: topic='{}', partition='{}', direction='{}'",
        session.getTopic(),
        session.getPartition(),
        topicRoute.getDirection()
    );

    // detect input participant
    int inputParticipant = topicRoute.getDirection() == Direction.OUT ? 1 : 2;

    // create request
    CommandRequest commandRequest = routingServiceCommander.createCommandRequest();
    commandRequest.target_router = targetRouter;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    commandRequest.command.entity_desc.name = String
        .format("Default::%s(%s)", session.getTopic(), session.getPartition());
    commandRequest.command.entity_desc.xml_url.content = String.format(
        "str://\"<auto_topic_route name=\"%1$s\"><input participant=\"%2$d\"><allow_topic_name_filter>%3$s</allow_topic_name_filter><datareader_qos base_name=\"QosLibrary::Base\"/></input><output><allow_topic_name_filter>%3$s</allow_topic_name_filter><datawriter_qos base_name=\"QosLibrary::Base\"/></output></auto_topic_route>\"",
        topicRoute.getDirection().toString(),
        inputParticipant,
        session.getTopic());
    commandRequest.command.entity_desc.xml_url.is_final = true;

    // send request
    CommandResponse reply = routingServiceCommander.sendRequest(
        commandRequest, REQUEST_TIMEOUT);

    // reply received?
    if (reply == null) {
      log.error(
          "No reply received when creating route for topic='{}', partition='{}', direction='{}'",
          session.getTopic(),
          session.getPartition(),
          topicRoute.getDirection().toString()
      );
      return;
    }
    // reply success or failed?
    if (reply.kind == CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK) {
      log.info(
          "Created route for topic='{}', partition='{}', direction='{}'",
          session.getTopic(),
          session.getPartition(),
          topicRoute.getDirection().toString()
      );
    } else {
      log.error(
          "Failed to create route for topic='{}', partition='{}', direction='{}', reason: {}, {}",
          session.getTopic(),
          session.getPartition(),
          topicRoute.getDirection().toString(),
          reply.kind,
          reply.message
      );
    }
  }

  @Override
  public void deleteTopicRoute(
      Session session,
      TopicRoute topicRoute
  ) {
    log.info(
        "Delete route: topic='{}', partition='{}', direction='{}'",
        session.getTopic(),
        session.getPartition(),
        topicRoute.getDirection()
    );

    // create request
    CommandRequest commandRequest = routingServiceCommander.createCommandRequest();
    commandRequest.target_router = targetRouter;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    commandRequest.command.entity_name = String.format(
        "Default::%1$s(%2$s)::%3$s", session.getTopic(), session.getPartition(), topicRoute.getDirection().toString());

    // send request
    CommandResponse reply = routingServiceCommander.sendRequest(
        commandRequest, REQUEST_TIMEOUT);

    // reply received?
    if (reply == null) {
      log.error(
          "No reply received when deleting route for topic='{}', partition='{}', direction='{}'",
          session.getTopic(),
          session.getPartition(),
          topicRoute.getDirection().toString()
      );
      return;
    }
    // reply success or failed?
    if (reply.kind == CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK) {
      log.info(
          "Deleted route for topic='{}', partition='{}', direction='{}'",
          session.getTopic(),
          session.getPartition(),
          topicRoute.getDirection().toString()
      );
    } else {
      log.error(
          "Failed to delete route for topic='{}', partition='{}', direction='{}', reason: {}, {}",
          session.getTopic(),
          session.getPartition(),
          topicRoute.getDirection().toString(),
          reply.kind,
          reply.message
      );
    }
  }
}
