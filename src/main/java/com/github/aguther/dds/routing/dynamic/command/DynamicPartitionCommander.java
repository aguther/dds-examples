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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.github.aguther.dds.routing.dynamic.observer.DynamicPartitionObserverListener;
import com.github.aguther.dds.routing.dynamic.observer.Session;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import com.github.aguther.dds.routing.util.RoutingServiceCommandInterface;
import com.google.common.base.Strings;
import idl.RTI.RoutingService.Administration.CommandKind;
import idl.RTI.RoutingService.Administration.CommandRequest;
import idl.RTI.RoutingService.Administration.CommandResponse;
import idl.RTI.RoutingService.Administration.CommandResponseKind;
import java.io.Closeable;
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

/**
 * This class commands a routing service to create or delete sessions and topic routes.
 *
 * It listens to a dynamic partition observer and when a session or topic route should be created or deleted it
 * creates, queues and sends the corresponding command to the target routing service.
 *
 * When a command is not successful, it retries the command after the retry delay until it worked or a converse
 * request (e.g. session creation vs. session deletion).
 */
public class DynamicPartitionCommander implements Closeable, DynamicPartitionObserverListener {

  private static final int DEFAULT_REQUEST_TIMEOUT_SECONDS = 10;
  private static final int DEFAULT_RETRY_DELAY_SECONDS = 10;

  private static final Logger LOGGER = LoggerFactory.getLogger(DynamicPartitionCommander.class);

  private final DynamicPartitionCommanderProvider dynamicPartitionCommanderProvider;
  private final RoutingServiceCommandInterface routingServiceCommandInterface;
  private final String targetRoutingService;

  private final ScheduledExecutorService executorService;
  private final Map<SimpleEntry<Session, TopicRoute>, ScheduledFuture> activeCommands;

  private final long requestTimeout;
  private final TimeUnit requestTimeoutTimeUnit;
  private final long retryDelay;
  private final TimeUnit retryDelayTimeUnit;

  /**
   * Instantiates a new Dynamic partition commander.
   *
   * @param routingServiceCommandInterface the routing service command helper
   * @param dynamicPartitionCommanderProvider the dynamic partition commander provider
   * @param targetRoutingService the target routing service
   */
  public DynamicPartitionCommander(
      final RoutingServiceCommandInterface routingServiceCommandInterface,
      final DynamicPartitionCommanderProvider dynamicPartitionCommanderProvider,
      final String targetRoutingService
  ) {
    this(
        routingServiceCommandInterface,
        dynamicPartitionCommanderProvider,
        targetRoutingService,
        DEFAULT_RETRY_DELAY_SECONDS,
        TimeUnit.SECONDS,
        DEFAULT_REQUEST_TIMEOUT_SECONDS,
        TimeUnit.SECONDS
    );
  }

  /**
   * Instantiates a new Dynamic partition commander.
   *
   * @param routingServiceCommandInterface the routing service command helper
   * @param dynamicPartitionCommanderProvider the dynamic partition commander provider
   * @param targetRoutingService the target routing service
   * @param retryDelay the retry delay
   * @param retryDelayTimeUnit the retry delay time unit
   */
  public DynamicPartitionCommander(
      final RoutingServiceCommandInterface routingServiceCommandInterface,
      final DynamicPartitionCommanderProvider dynamicPartitionCommanderProvider,
      final String targetRoutingService,
      final long retryDelay,
      final TimeUnit retryDelayTimeUnit
  ) {
    this(
        routingServiceCommandInterface,
        dynamicPartitionCommanderProvider,
        targetRoutingService,
        retryDelay,
        retryDelayTimeUnit,
        DEFAULT_REQUEST_TIMEOUT_SECONDS,
        TimeUnit.SECONDS
    );
  }

  /**
   * Instantiates a new Dynamic partition commander.
   *
   * @param routingServiceCommandInterface the routing service command helper
   * @param dynamicPartitionCommanderProvider the dynamic partition commander provider
   * @param targetRoutingService the target routing service
   * @param retryDelay the retry delay
   * @param retryDelayTimeUnit the retry delay time unit
   * @param requestTimeout the request timeout
   * @param requestTimeoutTimeUnit the request timeout time unit
   */
  public DynamicPartitionCommander(
      final RoutingServiceCommandInterface routingServiceCommandInterface,
      final DynamicPartitionCommanderProvider dynamicPartitionCommanderProvider,
      final String targetRoutingService,
      final long retryDelay,
      final TimeUnit retryDelayTimeUnit,
      final long requestTimeout,
      final TimeUnit requestTimeoutTimeUnit
  ) {
    checkNotNull(routingServiceCommandInterface, "Command interface must not be null.");
    checkNotNull(dynamicPartitionCommanderProvider, "Provider must not be null.");
    checkArgument(!Strings.isNullOrEmpty(targetRoutingService), "Target routing service must be valid.");
    checkArgument(retryDelay >= 0, "Retry delay is expected >= 0");
    checkArgument(requestTimeout > 0, "Timeout is expected > 0");

    this.routingServiceCommandInterface = routingServiceCommandInterface;
    this.dynamicPartitionCommanderProvider = dynamicPartitionCommanderProvider;
    this.targetRoutingService = targetRoutingService;

    activeCommands = Collections.synchronizedMap(new HashMap<>());

    executorService = Executors.newSingleThreadScheduledExecutor();

    this.requestTimeout = requestTimeout;
    this.requestTimeoutTimeUnit = requestTimeoutTimeUnit;
    this.retryDelay = retryDelay;
    this.retryDelayTimeUnit = retryDelayTimeUnit;
  }

  @Override
  public void close() {
    executorService.shutdownNow();
  }

  @Override
  public void createSession(
      final Session session
  ) {
    LOGGER.info(
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
      final Session session
  ) {
    LOGGER.info(
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
      final Session session,
      final TopicRoute topicRoute
  ) {
    LOGGER.info(
        "Create route: topic='{}', type='{}', partition='{}', direction='{}'",
        session.getTopic(),
        topicRoute.getType(),
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
      final Session session,
      final TopicRoute topicRoute
  ) {
    LOGGER.info(
        "Delete route: topic='{}', type='{}', partition='{}', direction='{}'",
        session.getTopic(),
        topicRoute.getType(),
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

  /**
   * Creates and sends a create session command.
   *
   * @param session session to create
   * @return true if session was successfully created, false if not
   */
  private boolean sendCreateSession(
      final Session session
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommandInterface.createCommandRequest();
    commandRequest.target_router = targetRoutingService;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    commandRequest.command.entity_desc.name = dynamicPartitionCommanderProvider.getSessionParent(session);
    commandRequest.command.entity_desc.xml_url.is_final = true;
    commandRequest.command.entity_desc.xml_url.content
        = dynamicPartitionCommanderProvider.getSessionConfiguration(session);

    // send request and return result
    return sendRequest(
        commandRequest,
        String.format(
            "entity='Session', topic='%s', partition='%s'",
            session.getTopic(),
            session.getPartition()
        )
    );
  }

  /**
   * Creates and sends a delete session command.
   *
   * @param session session to delete
   * @return true if session was successfully deleted, false if not
   */
  private boolean sendDeleteSession(
      final Session session
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommandInterface.createCommandRequest();
    commandRequest.target_router = targetRoutingService;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    commandRequest.command.entity_name = dynamicPartitionCommanderProvider.getSessionEntityName(session);

    // send request and return result
    return sendRequest(
        commandRequest,
        String.format(
            "entity='Session', topic='%s', partition='%s'",
            session.getTopic(),
            session.getPartition()
        )
    );
  }

  /**
   * Creates and sends a create topic route command.
   *
   * @param session session of topic route
   * @param topicRoute topic route to create
   * @return true if topic route was successfully created, false if not
   */
  private boolean sendCreateTopicRoute(
      final Session session,
      final TopicRoute topicRoute
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

    // send request and return result
    return sendRequest(
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

  /**
   * Creates and sends a delete topic route command.
   *
   * @param session session of topic route
   * @param topicRoute topic route to delete
   * @return true if topic route was successfully deleted, false if not
   */
  private boolean sendDeleteTopicRoute(
      final Session session,
      final TopicRoute topicRoute
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommandInterface.createCommandRequest();
    commandRequest.target_router = targetRoutingService;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    commandRequest.command.entity_name = dynamicPartitionCommanderProvider
        .getTopicRouteEntityName(session, topicRoute);

    // send request and return result
    return sendRequest(
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

  /**
   * Sends a request, waits for the result and checks it.
   *
   * @param commandRequest request to send
   * @param loggingFormat format string for logging
   * @return true if request was successful, false if not
   */
  private boolean sendRequest(
      final CommandRequest commandRequest,
      final String loggingFormat
  ) {
    // send request and get response
    CommandResponse commandResponse = routingServiceCommandInterface.sendRequest(
        commandRequest,
        requestTimeout,
        requestTimeoutTimeUnit
    );

    // check response
    return checkResponse(
        commandRequest,
        commandResponse,
        loggingFormat
    );
  }

  /**
   * Checks if a response was successful.
   *
   * @param commandRequest request that was sent
   * @param commandResponse response that was received
   * @param loggingFormat format string for logging
   * @return true if request was successful, false if not
   */
  private boolean checkResponse(
      final CommandRequest commandRequest,
      final CommandResponse commandResponse,
      final String loggingFormat
  ) {
    // response received?
    if (commandResponse == null) {
      LOGGER.error(
          "No response received request='{}', {}; retry in '{} {}'",
          commandRequest.command._d,
          loggingFormat,
          retryDelay,
          retryDelayTimeUnit
      );
      return false;
    }

    // success?
    if (commandResponse.kind == CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(
            "Success request='{}', {}",
            commandRequest.command._d,
            loggingFormat
        );
      }
      return true;
    }

    // failed
    LOGGER.error(
        "Failed request='{}', {}, reason='{}', message='{}'; retry in '{} {}'",
        commandRequest.command._d,
        loggingFormat,
        commandResponse.kind,
        commandResponse.message,
        retryDelay,
        retryDelayTimeUnit
    );
    return false;
  }
}
