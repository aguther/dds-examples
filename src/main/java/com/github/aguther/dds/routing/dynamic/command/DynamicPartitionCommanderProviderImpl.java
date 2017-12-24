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

import com.github.aguther.dds.routing.dynamic.observer.Session;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute.Direction;

/**
 * Provider with the following naming schemas:
 * - Sessions: "Topic(Partition)"
 * - Topic Routes: "OUT", "IN"
 *
 * A session's publisher and subscriber QoS is using the partition.
 * Topic routes are configured as "auto_topic_route".
 */
public class DynamicPartitionCommanderProviderImpl implements DynamicPartitionCommanderProvider {

  private String domainRouteName;

  public DynamicPartitionCommanderProviderImpl(
      String domainRouteName
  ) {
    this.domainRouteName = domainRouteName;
  }

  @Override
  public String getSessionParent(
      Session session
  ) {
    return domainRouteName;
  }

  @Override
  public String getSessionName(
      Session session
  ) {
    return String.format(
        "%s(%s)",
        session.getTopic(),
        session.getPartition()
    );
  }

  @Override
  public String getSessionEntityName(
      Session session
  ) {
    return String.format(
        "%s::%s",
        getSessionParent(session),
        getSessionName(session)
    );
  }

  @Override
  public String getSessionConfiguration(
      Session session
  ) {
    return String.format(
        "str://\"<session name=\"%1$s\"><publisher_qos><partition><name><element>%2$s</element></name></partition></publisher_qos><subscriber_qos><partition><name><element>%2$s</element></name></partition></subscriber_qos></session>\"",
        getSessionName(session),
        session.getPartition()
    );
  }

  @Override
  public String getTopicRouteName(
      Session session,
      TopicRoute topicRoute
  ) {
    return topicRoute.getDirection().toString();
  }

  @Override
  public String getTopicRouteEntityName(
      Session session,
      TopicRoute topicRoute
  ) {
    return String.format(
        "%s::%s",
        getSessionEntityName(session),
        getTopicRouteName(session, topicRoute)
    );
  }

  @Override
  public String getTopicRouteConfiguration(
      Session session,
      TopicRoute topicRoute
  ) {
    return String.format(
        "str://\"<auto_topic_route name=\"%1$s\"><input participant=\"%2$d\"><allow_topic_name_filter>%3$s</allow_topic_name_filter><datareader_qos base_name=\"QosLibrary::Base\"/></input><output><allow_topic_name_filter>%3$s</allow_topic_name_filter><datawriter_qos base_name=\"QosLibrary::Base\"/></output></auto_topic_route>\"",
        getTopicRouteName(session, topicRoute),
        topicRoute.getDirection() == Direction.OUT ? 1 : 2,
        session.getTopic()
    );
  }
}
