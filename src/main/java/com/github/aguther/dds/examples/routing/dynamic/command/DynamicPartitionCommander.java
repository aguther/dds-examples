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

package com.github.aguther.dds.examples.routing.dynamic.command;

import com.github.aguther.dds.util.RoutingServiceCommandHelper;
import com.github.aguther.dds.examples.routing.dynamic.observer.DynamicPartitionObserverListener;
import com.github.aguther.dds.examples.routing.dynamic.observer.Session;
import com.github.aguther.dds.examples.routing.dynamic.observer.TopicRoute;
import com.rti.dds.infrastructure.Duration_t;
import idl.RTI.RoutingService.Administration.CommandKind;
import idl.RTI.RoutingService.Administration.CommandRequest;
import idl.RTI.RoutingService.Administration.CommandResponse;
import idl.RTI.RoutingService.Administration.CommandResponseKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicPartitionCommander implements DynamicPartitionObserverListener {

  private static final Duration_t REQUEST_TIMEOUT;

  private static final Logger log;

  static {
    REQUEST_TIMEOUT = new Duration_t(10, 0);
    log = LoggerFactory.getLogger(DynamicPartitionCommander.class);
  }

  private final DynamicPartitionCommanderProvider dynamicPartitionCommanderProvider;
  private final RoutingServiceCommandHelper routingServiceCommandHelper;
  private final String targetRouter;


  public DynamicPartitionCommander(
      RoutingServiceCommandHelper routingServiceCommandHelper,
      DynamicPartitionCommanderProvider dynamicPartitionCommanderProvider,
      String targetRouter
  ) {
    this.routingServiceCommandHelper = routingServiceCommandHelper;
    this.dynamicPartitionCommanderProvider = dynamicPartitionCommanderProvider;
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
    CommandRequest commandRequest = routingServiceCommandHelper.createCommandRequest();
    commandRequest.target_router = targetRouter;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    commandRequest.command.entity_desc.name = dynamicPartitionCommanderProvider.getSessionParent(session);
    commandRequest.command.entity_desc.xml_url.is_final = true;
    commandRequest.command.entity_desc.xml_url.content
        = dynamicPartitionCommanderProvider.getSessionConfiguration(session);

    // send request
    CommandResponse reply = routingServiceCommandHelper.sendRequest(
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
      if (log.isInfoEnabled()) {
        log.info(
            "Created session for topic='{}', partition='{}'",
            session.getTopic(),
            session.getPartition()
        );
      }
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
    CommandRequest commandRequest = routingServiceCommandHelper.createCommandRequest();
    commandRequest.target_router = targetRouter;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    commandRequest.command.entity_name = dynamicPartitionCommanderProvider.getSessionEntityName(session);

    // send request
    CommandResponse reply = routingServiceCommandHelper.sendRequest(
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
      if (log.isInfoEnabled()) {
        log.info(
            "Deleted session for topic='{}', partition='{}'",
            session.getTopic(),
            session.getPartition()
        );
      }
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

    // create request
    CommandRequest commandRequest = routingServiceCommandHelper.createCommandRequest();
    commandRequest.target_router = targetRouter;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    commandRequest.command.entity_desc.name = dynamicPartitionCommanderProvider.getSessionEntityName(session);
    commandRequest.command.entity_desc.xml_url.is_final = true;
    commandRequest.command.entity_desc.xml_url.content
        = dynamicPartitionCommanderProvider.getTopicRouteConfiguration(session, topicRoute);

    // send request
    CommandResponse reply = routingServiceCommandHelper.sendRequest(
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
      if (log.isInfoEnabled()) {
        log.info(
            "Created route for topic='{}', partition='{}', direction='{}'",
            session.getTopic(),
            session.getPartition(),
            topicRoute.getDirection().toString()
        );
      }
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
    CommandRequest commandRequest = routingServiceCommandHelper.createCommandRequest();
    commandRequest.target_router = targetRouter;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    commandRequest.command.entity_name = dynamicPartitionCommanderProvider.getTopicRouteEntityName(session, topicRoute);

    // send request
    CommandResponse reply = routingServiceCommandHelper.sendRequest(
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
      if (log.isInfoEnabled()) {
        log.info(
            "Deleted route for topic='{}', partition='{}', direction='{}'",
            session.getTopic(),
            session.getPartition(),
            topicRoute.getDirection().toString()
        );
      }
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
