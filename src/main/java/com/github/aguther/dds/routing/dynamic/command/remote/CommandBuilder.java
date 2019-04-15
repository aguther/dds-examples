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
import idl.RTI.Service.Admin.CommandActionKind;
import idl.RTI.Service.Admin.CommandRequest;

class CommandBuilder {

  private final RoutingServiceCommandInterface routingServiceCommandInterface;
  private final String targetRoutingService;
  private final DynamicPartitionCommandProvider provider;

  CommandBuilder(
      RoutingServiceCommandInterface routingServiceCommandInterface,
      String targetRoutingService,
      DynamicPartitionCommandProvider dynamicPartitionCommandProvider
  ) {
    this.routingServiceCommandInterface = routingServiceCommandInterface;
    this.targetRoutingService = targetRoutingService;
    this.provider = dynamicPartitionCommandProvider;
  }

  Command buildCreateSessionCommand(
      Session session
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommandInterface.createCommandRequest();
    commandRequest.action = CommandActionKind.CREATE_ACTION;
    commandRequest.resource_identifier = String.format(
        "/routing_services/%s/domain_routes/%s",
        targetRoutingService,
        provider.getSessionParent(session)
    );
    commandRequest.string_body = String.format(
      "str://\"%s\"",
      provider.getSessionConfiguration(session)
    );

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
    commandRequest.action = CommandActionKind.DELETE_ACTION;
    commandRequest.resource_identifier = String.format(
        "/routing_services/%s/domain_routes/%s/sessions/%s",
        targetRoutingService,
        provider.getSessionParent(session),
        provider.getSessionName(session)
    );

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
    commandRequest.action = CommandActionKind.CREATE_ACTION;
    commandRequest.resource_identifier = String.format(
        "/routing_services/%s/domain_routes/%s/sessions/%s",
        targetRoutingService,
        provider.getSessionParent(session),
        provider.getSessionName(session)
    );
    commandRequest.string_body = String.format(
      "str://\"%s\"",
      provider.getTopicRouteConfiguration(session, topicRoute)
    );

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
    commandRequest.action = CommandActionKind.DELETE_ACTION;
    commandRequest.resource_identifier = String.format(
        "/routing_services/%s/domain_routes/%s/sessions/%s/routes/%s",
        targetRoutingService,
        provider.getSessionParent(session),
        provider.getSessionName(session),
        provider.getTopicRouteName(session, topicRoute)
    );

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
