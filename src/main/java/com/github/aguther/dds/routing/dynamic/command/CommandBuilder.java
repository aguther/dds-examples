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

package com.github.aguther.dds.routing.dynamic.command;

import com.github.aguther.dds.routing.dynamic.observer.Session;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import com.github.aguther.dds.routing.util.RoutingServiceCommandInterface;
import idl.RTI.RoutingService.Administration.CommandKind;
import idl.RTI.RoutingService.Administration.CommandRequest;

public class CommandBuilder {

  private final RoutingServiceCommandInterface routingServiceCommandInterface;
  private final String targetRoutingService;
  private final DynamicPartitionCommanderProvider dynamicPartitionCommanderProvider;

  public CommandBuilder(
      RoutingServiceCommandInterface routingServiceCommandInterface,
      String targetRoutingService,
      DynamicPartitionCommanderProvider dynamicPartitionCommanderProvider
  ) {
    this.routingServiceCommandInterface = routingServiceCommandInterface;
    this.targetRoutingService = targetRoutingService;
    this.dynamicPartitionCommanderProvider = dynamicPartitionCommanderProvider;
  }

  public Command buildCreateSessionCommand(
      Session session
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommandInterface.createCommandRequest();
    commandRequest.target_router = targetRoutingService;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    commandRequest.command.entity_desc.name = dynamicPartitionCommanderProvider.getSessionParent(session);
    commandRequest.command.entity_desc.xml_url.is_final = true;
    commandRequest.command.entity_desc.xml_url.content
        = dynamicPartitionCommanderProvider.getSessionConfiguration(session);

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

  public Command buildDeleteSessionCommand(
      Session session
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommandInterface.createCommandRequest();
    commandRequest.target_router = targetRoutingService;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    commandRequest.command.entity_name = dynamicPartitionCommanderProvider.getSessionEntityName(session);

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

  public Command buildCreateTopicRouteCommand(
      Session session,
      TopicRoute topicRoute
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommandInterface.createCommandRequest();
    commandRequest.target_router = targetRoutingService;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    commandRequest.command.entity_desc.name = dynamicPartitionCommanderProvider
        .getSessionEntityName(session);
    commandRequest.command.entity_desc.xml_url.is_final = true;
    commandRequest.command.entity_desc.xml_url.content
        = dynamicPartitionCommanderProvider.getTopicRouteConfiguration(session, topicRoute);

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

  public Command buildDeleteTopicRouteCommand(
      Session session,
      TopicRoute topicRoute
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommandInterface.createCommandRequest();
    commandRequest.target_router = targetRoutingService;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    commandRequest.command.entity_name
        = dynamicPartitionCommanderProvider.getTopicRouteEntityName(session, topicRoute);

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
