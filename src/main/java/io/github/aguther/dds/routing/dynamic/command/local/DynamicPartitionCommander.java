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

package io.github.aguther.dds.routing.dynamic.command.local;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.aguther.dds.routing.dynamic.command.DynamicPartitionCommandProvider;
import io.github.aguther.dds.routing.dynamic.observer.DynamicPartitionObserverListener;
import io.github.aguther.dds.routing.dynamic.observer.Session;
import io.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import com.rti.routingservice.RoutingService;
import com.rti.routingservice.infrastructure.RoutingServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class commands a routing service (running as library) to create or delete sessions and topic routes.
 *
 * It listens to a dynamic partition observer and when a session or topic route should be created or deleted it
 * creates, queues and invokes appropriate functions on the routing service library.
 */
public class DynamicPartitionCommander implements DynamicPartitionObserverListener {

  private static final Logger LOGGER = LogManager.getLogger(
    DynamicPartitionCommander.class);

  private final RoutingService routingService;
  private final DynamicPartitionCommandProvider dynamicPartitionCommandProvider;

  /**
   * Instantiates a new Dynamic partition commander.
   *
   * @param routingService the routing service instance to command
   */
  public DynamicPartitionCommander(
    final RoutingService routingService,
    final DynamicPartitionCommandProvider dynamicPartitionCommandProvider
  ) {
    checkNotNull(routingService, "Routing Service must not be null.");
    checkNotNull(dynamicPartitionCommandProvider, "DynamicPartitionCommandProvider must not be null.");

    this.routingService = routingService;
    this.dynamicPartitionCommandProvider = dynamicPartitionCommandProvider;
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

    // creation of session
    try {
      routingService.createEntity(
        dynamicPartitionCommandProvider.getSessionParent(session),
        dynamicPartitionCommandProvider.getSessionConfiguration(session)
      );
    } catch (RoutingServiceException ex) {
      LOGGER.error(
        "Failed to create session: topic='{}', partition='{}', message='{}'",
        session.getTopic(),
        session.getPartition(),
        ex.getMessage()
      );
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

    // deletion of session
    try {
      routingService.deleteEntity(
        dynamicPartitionCommandProvider.getSessionEntityName(session)
      );
    } catch (RoutingServiceException ex) {
      LOGGER.error(
        "Failed to delete session: topic='{}', partition='{}', message='{}'",
        session.getTopic(),
        session.getPartition(),
        ex.getMessage()
      );
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

    // creation of topic route
    try {
      routingService.createEntity(
        dynamicPartitionCommandProvider.getSessionEntityName(session),
        dynamicPartitionCommandProvider.getTopicRouteConfiguration(session, topicRoute)
      );
    } catch (RoutingServiceException ex) {
      LOGGER.error(
        "Failed to create route: topic='{}', type='{}', partition='{}', direction='{}', message='{}'",
        session.getTopic(),
        topicRoute.getType(),
        session.getPartition(),
        topicRoute.getDirection(),
        ex.getMessage()
      );
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

    // deletion of topic route
    try {
      routingService.deleteEntity(
        dynamicPartitionCommandProvider.getTopicRouteEntityName(session, topicRoute)
      );
    } catch (RoutingServiceException ex) {
      LOGGER.error(
        "Failed to create route: topic='{}', type='{}', partition='{}', direction='{}', message='{}'",
        session.getTopic(),
        topicRoute.getType(),
        session.getPartition(),
        topicRoute.getDirection(),
        ex.getMessage()
      );
    }
  }
}
