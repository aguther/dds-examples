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

package com.github.aguther.dds.routing.dynamic.command;

import com.github.aguther.dds.routing.dynamic.observer.DynamicPartitionObserverListener;
import com.github.aguther.dds.routing.dynamic.observer.Session;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import com.github.aguther.dds.util.RoutingServiceCommandHelper;
import idl.RTI.RoutingService.Administration.CommandKind;
import idl.RTI.RoutingService.Administration.CommandRequest;
import idl.RTI.RoutingService.Administration.CommandResponse;
import idl.RTI.RoutingService.Administration.CommandResponseKind;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicPartitionCommander implements DynamicPartitionObserverListener {

  private static final int DEFAULT_REQUEST_TIMEOUT_SECONDS;
  private static final int DEFAULT_RETRY_DELAY_SECONDS;

  private static final Logger log;

  static {
    DEFAULT_REQUEST_TIMEOUT_SECONDS = 10;
    DEFAULT_RETRY_DELAY_SECONDS = 10;

    log = LoggerFactory.getLogger(DynamicPartitionCommander.class);
  }

  private final DynamicPartitionCommanderProvider dynamicPartitionCommanderProvider;
  private final RoutingServiceCommandHelper routingServiceCommandHelper;
  private final String targetRouter;

  private final ScheduledExecutorService executorService;
  private final Map<SimpleEntry<Session, TopicRoute>, ScheduledFuture> activeCommands;

  private long requestTimeout;
  private TimeUnit requestTimeoutTimeUnit;
  private long retryDelay;
  private TimeUnit retryDelayTimeUnit;

  public DynamicPartitionCommander(
      RoutingServiceCommandHelper routingServiceCommandHelper,
      DynamicPartitionCommanderProvider dynamicPartitionCommanderProvider,
      String targetRouter
  ) {
    this(
        routingServiceCommandHelper,
        dynamicPartitionCommanderProvider,
        targetRouter,
        DEFAULT_RETRY_DELAY_SECONDS,
        TimeUnit.SECONDS,
        DEFAULT_REQUEST_TIMEOUT_SECONDS,
        TimeUnit.SECONDS
    );
  }

  public DynamicPartitionCommander(
      RoutingServiceCommandHelper routingServiceCommandHelper,
      DynamicPartitionCommanderProvider dynamicPartitionCommanderProvider,
      String targetRouter,
      long retryDelay,
      TimeUnit retryDelayTimeUnit
  ) {
    this(
        routingServiceCommandHelper,
        dynamicPartitionCommanderProvider,
        targetRouter,
        retryDelay,
        retryDelayTimeUnit,
        DEFAULT_REQUEST_TIMEOUT_SECONDS,
        TimeUnit.SECONDS
    );
  }

  public DynamicPartitionCommander(
      RoutingServiceCommandHelper routingServiceCommandHelper,
      DynamicPartitionCommanderProvider dynamicPartitionCommanderProvider,
      String targetRouter,
      long retryDelay,
      TimeUnit retryDelayTimeUnit,
      long requestTimeout,
      TimeUnit requestTimeoutTimeUnit
  ) {
    this.routingServiceCommandHelper = routingServiceCommandHelper;
    this.dynamicPartitionCommanderProvider = dynamicPartitionCommanderProvider;
    this.targetRouter = targetRouter;

    activeCommands = Collections.synchronizedMap(new HashMap<>());

    executorService = Executors.newSingleThreadScheduledExecutor();

    this.requestTimeout = requestTimeout;
    this.requestTimeoutTimeUnit = requestTimeoutTimeUnit;
    this.retryDelay = retryDelay;
    this.retryDelayTimeUnit = retryDelayTimeUnit;
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

    synchronized (activeCommands) {
      // create command
      SimpleEntry<Session, TopicRoute> command = new SimpleEntry<>(session, null);

      // when another command is scheduled, cancel it
      if (activeCommands.containsKey(command)) {
        activeCommands.remove(command).cancel(false);
      }

      // schedule creation of session
      ScheduledFuture commandFuture = executorService.scheduleWithFixedDelay(
          () -> {
            if (sendCreateSession(session)) {
              activeCommands.remove(command).cancel(false);
            }
          },
          0,
          retryDelay,
          retryDelayTimeUnit
      );

      // add command to scheduled commands
      activeCommands.put(command, commandFuture);
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

    synchronized (activeCommands) {
      // create command
      SimpleEntry<Session, TopicRoute> command = new SimpleEntry<>(session, null);

      // when another command is scheduled, cancel it
      if (activeCommands.containsKey(command)) {
        activeCommands.remove(command).cancel(false);
      }

      // schedule creation of session
      ScheduledFuture commandFuture = executorService.scheduleWithFixedDelay(
          () -> {
            if (sendDeleteSession(session)) {
              activeCommands.remove(command).cancel(false);
            }
          },
          0,
          retryDelay,
          retryDelayTimeUnit
      );

      // add command to scheduled commands
      activeCommands.put(command, commandFuture);
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

    synchronized (activeCommands) {
      // create command
      SimpleEntry<Session, TopicRoute> command = new SimpleEntry<>(session, topicRoute);

      // when another command is scheduled, cancel it
      if (activeCommands.containsKey(command)) {
        activeCommands.remove(command).cancel(false);
      }

      // schedule creation of session
      ScheduledFuture commandFuture = executorService.scheduleWithFixedDelay(
          () -> {
            if (sendCreateTopicRoute(session, topicRoute)) {
              activeCommands.remove(command).cancel(false);
            }
          },
          0,
          retryDelay,
          retryDelayTimeUnit
      );

      // add command to scheduled commands
      activeCommands.put(command, commandFuture);
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

    synchronized (activeCommands) {
      // create command
      SimpleEntry<Session, TopicRoute> command = new SimpleEntry<>(session, topicRoute);

      // when another command is scheduled, cancel it
      if (activeCommands.containsKey(command)) {
        activeCommands.remove(command).cancel(false);
      }

      // schedule creation of session
      ScheduledFuture commandFuture = executorService.scheduleWithFixedDelay(
          () -> {
            if (sendDeleteTopicRoute(session, topicRoute)) {
              activeCommands.remove(command).cancel(false);
            }
          },
          0,
          retryDelay,
          retryDelayTimeUnit
      );

      // add command to scheduled commands
      activeCommands.put(command, commandFuture);
    }
  }

  private boolean sendCreateSession(
      Session session
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommandHelper.createCommandRequest();
    commandRequest.target_router = targetRouter;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    commandRequest.command.entity_desc.name = dynamicPartitionCommanderProvider.getSessionParent(session);
    commandRequest.command.entity_desc.xml_url.is_final = true;
    commandRequest.command.entity_desc.xml_url.content
        = dynamicPartitionCommanderProvider.getSessionConfiguration(session);

    // send request and return result
    return sendRequest(
        commandRequest,
        String.format(
            "topic='%s', partition='%s'",
            session.getTopic(),
            session.getPartition()
        )
    );
  }

  private boolean sendDeleteSession(
      Session session
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommandHelper.createCommandRequest();
    commandRequest.target_router = targetRouter;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    commandRequest.command.entity_name = dynamicPartitionCommanderProvider.getSessionEntityName(session);

    // send request and return result
    return sendRequest(
        commandRequest,
        String.format(
            "topic='%s', partition='%s'",
            session.getTopic(),
            session.getPartition()
        )
    );
  }

  private boolean sendCreateTopicRoute(
      Session session,
      TopicRoute topicRoute
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommandHelper.createCommandRequest();
    commandRequest.target_router = targetRouter;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    commandRequest.command.entity_desc.name = dynamicPartitionCommanderProvider
        .getSessionEntityName(session);
    commandRequest.command.entity_desc.xml_url.is_final = true;
    commandRequest.command.entity_desc.xml_url.content
        = dynamicPartitionCommanderProvider.getTopicRouteConfiguration(session, topicRoute);

    // send request and return result
    return sendRequest(
        commandRequest,
        String.format(
            "topic='%s', partition='%s', direction='%s'",
            session.getTopic(),
            session.getPartition(),
            topicRoute.getDirection().toString()
        )
    );
  }

  private boolean sendDeleteTopicRoute(
      Session session,
      TopicRoute topicRoute
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommandHelper.createCommandRequest();
    commandRequest.target_router = targetRouter;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    commandRequest.command.entity_name = dynamicPartitionCommanderProvider
        .getTopicRouteEntityName(session, topicRoute);

    // send request and return result
    return sendRequest(
        commandRequest,
        String.format(
            "topic='%s', partition='%s', direction='%s'",
            session.getTopic(),
            session.getPartition(),
            topicRoute.getDirection().toString()
        )
    );
  }

  private boolean sendRequest(
      CommandRequest commandRequest,
      String identification
  ) {
    // send request and get response
    CommandResponse commandResponse = routingServiceCommandHelper.sendRequest(
        commandRequest,
        requestTimeout,
        requestTimeoutTimeUnit
    );

    // check response
    return checkResponse(
        commandRequest,
        commandResponse,
        identification
    );
  }

  private boolean checkResponse(
      CommandRequest commandRequest,
      CommandResponse commandResponse,
      String identification
  ) {
    // response received?
    if (commandResponse == null) {
      log.error(
          "No response received request='{}', {}",
          commandRequest.command._d,
          identification
      );
      return false;
    }

    // success?
    if (commandResponse.kind == CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK) {
      if (log.isInfoEnabled()) {
        log.info(
            "Success request='{}', {}",
            commandRequest.command._d,
            identification
        );
      }
      return true;
    }

    // failed
    log.error(
        "Failed request='{}', {}, reason='{}', message='{}'",
        commandRequest.command._d,
        identification,
        commandResponse.kind,
        commandResponse.message
    );
    return false;
  }
}
