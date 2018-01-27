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

package com.github.aguther.dds.routing.dynamic.command.remote;

import com.github.aguther.dds.routing.dynamic.command.DynamicPartitionCommandProvider;
import com.github.aguther.dds.routing.dynamic.observer.Session;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import com.github.aguther.dds.routing.util.RoutingServiceCommandInterface;
import idl.RTI.RoutingService.Administration.CommandKind;
import idl.RTI.RoutingService.Administration.CommandRequest;

class CommandBuilder {

  private final RoutingServiceCommandInterface routingServiceCommandInterface;
  private final String targetRoutingService;
  private final DynamicPartitionCommandProvider dynamicPartitionCommandProvider;

  CommandBuilder(
      RoutingServiceCommandInterface routingServiceCommandInterface,
      String targetRoutingService,
      DynamicPartitionCommandProvider dynamicPartitionCommandProvider
  ) {
    this.routingServiceCommandInterface = routingServiceCommandInterface;
    this.targetRoutingService = targetRoutingService;
    this.dynamicPartitionCommandProvider = dynamicPartitionCommandProvider;
  }

  Command buildCreateSessionCommand(
      Session session
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommandInterface.createCommandRequest();
    commandRequest.target_router = targetRoutingService;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    commandRequest.command.entity_desc.name = dynamicPartitionCommandProvider.getSessionParent(session);
    commandRequest.command.entity_desc.xml_url.is_final = true;
    commandRequest.command.entity_desc.xml_url.content
        = dynamicPartitionCommandProvider.getSessionConfiguration(session);

    // create and return command
    return new Command(
        CommandType.COMMAND_TYPE_CREATE,
        session,
        null,
        commandRequest,
        String.format(
            "entity='Session', topic='%s', partition='%s'",
            session.getTopic(),
            session.getPartition()
        )
    );
  }

  Command buildDeleteSessionCommand(
      Session session
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommandInterface.createCommandRequest();
    commandRequest.target_router = targetRoutingService;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    commandRequest.command.entity_name = dynamicPartitionCommandProvider.getSessionEntityName(session);

    // create and return command
    return new Command(
        CommandType.COMMAND_TYPE_DELETE,
        session,
        null,
        commandRequest,
        String.format(
            "entity='Session', topic='%s', partition='%s'",
            session.getTopic(),
            session.getPartition()
        )
    );
  }

  Command buildCreateTopicRouteCommand(
      Session session,
      TopicRoute topicRoute
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommandInterface.createCommandRequest();
    commandRequest.target_router = targetRoutingService;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    commandRequest.command.entity_desc.name = dynamicPartitionCommandProvider
        .getSessionEntityName(session);
    commandRequest.command.entity_desc.xml_url.is_final = true;
    commandRequest.command.entity_desc.xml_url.content
        = dynamicPartitionCommandProvider.getTopicRouteConfiguration(session, topicRoute);

    // create and return command
    return new Command(
        CommandType.COMMAND_TYPE_CREATE,
        session,
        topicRoute,
        commandRequest,
        String.format(
            "entity='TopicRoute', topic='%s', type='%s', partition='%s', direction='%s'",
            session.getTopic(),
            topicRoute.getType(),
            session.getPartition(),
            topicRoute.getDirection().toString()
        )
    );
  }

  Command buildDeleteTopicRouteCommand(
      Session session,
      TopicRoute topicRoute
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommandInterface.createCommandRequest();
    commandRequest.target_router = targetRoutingService;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    commandRequest.command.entity_name
        = dynamicPartitionCommandProvider.getTopicRouteEntityName(session, topicRoute);

    // create and return command
    return new Command(
        CommandType.COMMAND_TYPE_DELETE,
        session,
        topicRoute,
        commandRequest,
        String.format(
            "entity='TopicRoute', topic='%s', type='%s', partition='%s', direction='%s'",
            session.getTopic(),
            topicRoute.getType(),
            session.getPartition(),
            topicRoute.getDirection().toString()
        )
    );
  }
}
