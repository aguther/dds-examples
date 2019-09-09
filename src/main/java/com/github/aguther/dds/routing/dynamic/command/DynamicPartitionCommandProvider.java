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

/**
 * Callback interface to get information and configuration for the creation and deletion of sessions and topic routes.
 */
public interface DynamicPartitionCommandProvider {

  /**
   * Invoked to get the parent entity name of a session.
   *
   * @param session session for which the parent is needed
   * @apiNote "ExampleDomainRoute"
   */
  String getSessionParent(
    final Session session
  );

  /**
   * Invoked to get the name of a session.
   *
   * @param session session for which the name is needed
   * @apiNote "ExampleSession(Partition)"
   */
  String getSessionName(
    final Session session
  );

  /**
   * Invoked to get the full entity name of a session including the parents.
   * Must be in-line with the routing service API.
   *
   * @param session session for which the full entity name is needed
   * @apiNote "ExampleDomainRoute::ExampleSession(Partition)"
   */
  String getSessionEntityName(
    final Session session
  );

  /**
   * Invoked to get the configuration of a session.
   *
   * @param session session for which the configuration is needed
   * @apiNote "str://"<session name=\"ExampleSession(Partition)\">[...]</session>"
   */
  String getSessionConfiguration(
    final Session session
  );

  /**
   * Invoked to get the name of a topic route.
   *
   * @param session session of topic route
   * @param topicRoute topic route for which the full entity name is needed
   * @apiNote "ExampleTopicRoute"
   */
  String getTopicRouteName(
    final Session session,
    final TopicRoute topicRoute
  );

  /**
   * Invoked to get the full entity name of a topic route including the parents.
   * Must be in-line with the routing service API.
   *
   * @param session session of topic route
   * @param topicRoute topic route for which the full entity name is needed
   * @apiNote "ExampleDomainRoute::ExampleSession(Partition)::ExampleTopicRoute"
   */
  String getTopicRouteEntityName(
    final Session session,
    final TopicRoute topicRoute
  );

  /**
   * Invoked to get the configuration of a topic route.
   *
   * @param session session of topic route
   * @param topicRoute topic route for which the configuration is needed
   * @apiNote "str://\"<auto_topic_route name=\"ExampleTopicRoute\">[...]</auto_topic_route>\""
   */
  String getTopicRouteConfiguration(
    final Session session,
    final TopicRoute topicRoute
  );
}
