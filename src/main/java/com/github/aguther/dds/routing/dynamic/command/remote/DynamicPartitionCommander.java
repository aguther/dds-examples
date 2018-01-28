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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.github.aguther.dds.routing.dynamic.command.DynamicPartitionCommandProvider;
import com.github.aguther.dds.routing.dynamic.observer.DynamicPartitionObserverListener;
import com.github.aguther.dds.routing.dynamic.observer.Session;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import com.github.aguther.dds.routing.util.RoutingServiceCommandInterface;
import com.google.common.base.Strings;
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
import java.util.concurrent.TimeUnit;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.FailsafeFuture;
import net.jodah.failsafe.RetryPolicy;
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

  private final RoutingServiceCommandInterface routingServiceCommandInterface;

  private final CommandBuilder commandBuilder;

  private final ScheduledExecutorService executorService;
  private final Map<SimpleEntry<Session, TopicRoute>, SimpleEntry<FailsafeFuture, Command>> scheduledCommands;

  private final RetryPolicy retryPolicy;
  private final RetryPolicy retryPolicyAfterRetry;

  private final long requestTimeout;
  private final TimeUnit requestTimeoutTimeUnit;
  private final long retryDelay;
  private final TimeUnit retryDelayTimeUnit;

  /**
   * Instantiates a new Dynamic partition commander.
   *
   * @param routingServiceCommandInterface the routing service command helper
   * @param dynamicPartitionCommandProvider the dynamic partition commander provider
   * @param targetRoutingService the target routing service
   */
  public DynamicPartitionCommander(
      final RoutingServiceCommandInterface routingServiceCommandInterface,
      final DynamicPartitionCommandProvider dynamicPartitionCommandProvider,
      final String targetRoutingService
  ) {
    this(
        routingServiceCommandInterface,
        dynamicPartitionCommandProvider,
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
   * @param dynamicPartitionCommandProvider the dynamic partition commander provider
   * @param targetRoutingService the target routing service
   * @param retryDelay the retry delay
   * @param retryDelayTimeUnit the retry delay time unit
   */
  public DynamicPartitionCommander(
      final RoutingServiceCommandInterface routingServiceCommandInterface,
      final DynamicPartitionCommandProvider dynamicPartitionCommandProvider,
      final String targetRoutingService,
      final long retryDelay,
      final TimeUnit retryDelayTimeUnit
  ) {
    this(
        routingServiceCommandInterface,
        dynamicPartitionCommandProvider,
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
   * @param dynamicPartitionCommandProvider the dynamic partition commander provider
   * @param targetRoutingService the target routing service
   * @param retryDelay the retry delay
   * @param retryDelayTimeUnit the retry delay time unit
   * @param requestTimeout the request timeout
   * @param requestTimeoutTimeUnit the request timeout time unit
   */
  public DynamicPartitionCommander(
      final RoutingServiceCommandInterface routingServiceCommandInterface,
      final DynamicPartitionCommandProvider dynamicPartitionCommandProvider,
      final String targetRoutingService,
      final long retryDelay,
      final TimeUnit retryDelayTimeUnit,
      final long requestTimeout,
      final TimeUnit requestTimeoutTimeUnit
  ) {
    checkNotNull(routingServiceCommandInterface, "Command interface must not be null.");
    checkNotNull(dynamicPartitionCommandProvider, "Provider must not be null.");
    checkArgument(!Strings.isNullOrEmpty(targetRoutingService), "Target routing service must be valid.");
    checkArgument(retryDelay > 0, "Retry delay is expected > 0");
    checkNotNull(retryDelayTimeUnit, "Retry time unit must not be null.");
    checkArgument(requestTimeout > 0, "Timeout is expected > 0");
    checkNotNull(requestTimeoutTimeUnit, "Request timeout unit must not be null.");

    this.routingServiceCommandInterface = routingServiceCommandInterface;
    this.retryDelay = retryDelay;
    this.retryDelayTimeUnit = retryDelayTimeUnit;
    this.requestTimeout = requestTimeout;
    this.requestTimeoutTimeUnit = requestTimeoutTimeUnit;

    commandBuilder = new CommandBuilder(
        routingServiceCommandInterface,
        targetRoutingService,
        dynamicPartitionCommandProvider
    );

    scheduledCommands = Collections.synchronizedMap(new HashMap<>());

    executorService = Executors.newSingleThreadScheduledExecutor();

    retryPolicy = new RetryPolicy()
        .retryWhen(false)
        .withDelay(retryDelay, retryDelayTimeUnit);

    retryPolicyAfterRetry = new RetryPolicy();
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

    // schedule creation of session
    scheduleCommand(commandBuilder.buildCreateSessionCommand(session));
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

    // schedule deletion of session
    scheduleCommand(commandBuilder.buildDeleteSessionCommand(session));
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

    // schedule creation of topic route
    scheduleCommand(commandBuilder.buildCreateTopicRouteCommand(session, topicRoute));
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

    // schedule deletion of topic route
    scheduleCommand(commandBuilder.buildDeleteTopicRouteCommand(session, topicRoute));
  }

  /*
   * The following cases are foreseen when scheduling commands:
   *
   * Assumption for failed => Error | Timeout
   *
   * Case 1: A -> run -> success -> end
   * Case 2: A -> run -> failed -> wait retry -> run -> success -> end
   * Case 3: A -> run -> failed -> wait retry -> B -> abort (A) -> restart (B=>A)
   * Case 4: A -> run -> B -> abort (A) -> success (A) -> restart (B=>A)
   * Case 5: A -> run -> B -> abort (A) -> failed (A) -> end
   */

  /**
   * Schedules a command.
   *
   * @param command command to be scheduled
   */
  private void scheduleCommand(
      Command command
  ) {
    // create entry for command
    SimpleEntry<Session, TopicRoute> commandKey = new SimpleEntry<>(
        command.getSession(), command.getTopicRoute());

    // select default retry policy
    RetryPolicy appliedRetryPolicy = retryPolicy;

    // get previous entry
    SimpleEntry<FailsafeFuture, Command> previousCommandKey = scheduledCommands.remove(commandKey);
    // check if previous command is still active
    if (previousCommandKey != null) {
      // abort previous command
      previousCommandKey.getKey().cancel(false);
      // override retry policy
      appliedRetryPolicy = retryPolicyAfterRetry;
    }

    // schedule creation of session
    FailsafeFuture commandFuture = Failsafe
        .with(appliedRetryPolicy)
        .with(executorService)
        .onSuccess(result -> scheduledCommands.remove(commandKey))
        .get(() -> sendRequest(command));

    // add command to scheduled commands
    scheduledCommands.put(commandKey, new SimpleEntry<>(commandFuture, command));
  }

  /**
   * Sends a request, waits for the result and checks it.
   *
   * @param command command to send
   * @return true if request was successful, false if not
   */
  private boolean sendRequest(
      final Command command
  ) {
    // get command request (this needs to be done, otherwise this function is somehow called twice)
    CommandRequest commandRequest = command.getCommandRequest();

    // send request and get response
    CommandResponse commandResponse = routingServiceCommandInterface.sendRequest(
        commandRequest,
        requestTimeout,
        requestTimeoutTimeUnit
    );

    // check response
    return checkResponse(
        command.getCommandRequest(),
        commandResponse,
        command.getLoggingFormat()
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
