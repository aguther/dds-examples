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

package com.github.aguther.dds.examples.discovery;

import com.github.aguther.dds.examples.discovery.TopicRoute.Direction;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.StringSeq;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteObserver implements PublicationObserverListener, SubscriptionObserverListener {

  private static final Logger log;

  static {
    log = LoggerFactory.getLogger(RouteObserver.class);
  }

  private final Object mappingLock;
  private final HashMap<String, Multimap<TopicRoute, InstanceHandle_t>> mapping;

  public RouteObserver() {
    mappingLock = new Object();
    mapping = new HashMap<>();
  }

  @Override
  public void publicationDiscovered(
      InstanceHandle_t instanceHandle,
      PublicationBuiltinTopicData data
  ) {
    handleDiscovered(
        instanceHandle,
        Direction.OUT,
        data.topic_name,
        data.type_name,
        data.partition.name
    );
  }

  @Override
  public void publicationLost(
      InstanceHandle_t instanceHandle,
      PublicationBuiltinTopicData data
  ) {
    handleLost(
        instanceHandle,
        Direction.OUT,
        data.topic_name,
        data.type_name,
        data.partition.name
    );
  }

  @Override
  public void subscriptionDiscovered(
      InstanceHandle_t instanceHandle,
      SubscriptionBuiltinTopicData data
  ) {
    handleDiscovered(
        instanceHandle,
        Direction.IN,
        data.topic_name,
        data.type_name,
        data.partition.name
    );
  }

  @Override
  public void subscriptionLost(
      InstanceHandle_t instanceHandle,
      SubscriptionBuiltinTopicData data
  ) {
    handleLost(
        instanceHandle,
        Direction.IN,
        data.topic_name,
        data.type_name,
        data.partition.name
    );
  }

  private void handleDiscovered(
      InstanceHandle_t instanceHandle,
      Direction direction,
      String topicName,
      String typeName,
      StringSeq partitions
  ) {
    synchronized (mappingLock) {
      // create topic session if first item discovered
      if (!mapping.containsKey(topicName)) {
        mapping.put(topicName, ArrayListMultimap.create());
      }

      // create routes for all partitions we discovered
      if (partitions.isEmpty()) {
        // create topic route object
        TopicRoute topicRoute = new TopicRoute(
            direction,
            topicName,
            typeName,
            ""
        );

        // check if topic route is about to be created
        if (!mapping.get(topicName).containsKey(topicRoute)) {
          log.info(
              "New topic route: direction='{}', topic='{}', type='{}', partition='{}'",
              topicRoute.getDirection(),
              topicRoute.getTopic(),
              topicRoute.getType(),
              topicRoute.getPartition()
          );
        }

        // add instance handle to topic route
        mapping.get(topicName).put(topicRoute, instanceHandle);
      } else {
        for (Object partition : partitions) {
          // create topic route object
          TopicRoute topicRoute = new TopicRoute(
              direction,
              topicName,
              typeName,
              partition.toString()
          );

          // check if topic route is about to be created
          if (!mapping.get(topicName).containsKey(topicRoute)) {
            log.info(
                "New topic route: direction='{}', topic='{}', type='{}', partition='{}'",
                topicRoute.getDirection(),
                topicRoute.getTopic(),
                topicRoute.getType(),
                topicRoute.getPartition()
            );
          }

          // add instance handle to topic route
          mapping.get(topicName).put(topicRoute, instanceHandle);
        }
      }
    }
  }

  private void handleLost(
      InstanceHandle_t instanceHandle,
      Direction direction,
      String topicName,
      String typeName,
      StringSeq partitions
  ) {
    synchronized (mappingLock) {
      // delete routes for all partitions we lost
      if (partitions.isEmpty()) {
        // create topic route object
        TopicRoute topicRoute = new TopicRoute(
            direction,
            topicName,
            typeName,
            ""
        );

        // remove instance handle from topic route
        mapping.get(topicName).put(topicRoute, instanceHandle);

        // check if route was deleted
        if (!mapping.get(topicName).containsKey(topicRoute)) {
          log.info(
              "Delete topic route: direction='{}', topic='{}', type='{}', partition='{}'",
              topicRoute.getDirection(),
              topicRoute.getTopic(),
              topicRoute.getType(),
              topicRoute.getPartition()
          );
        }
      } else {
        for (Object partition : partitions) {
          // create topic route object
          TopicRoute topicRoute = new TopicRoute(
              direction,
              topicName,
              typeName,
              partition.toString()
          );

          // remove instance handle from topic route
          mapping.get(topicName).remove(topicRoute, instanceHandle);

          // check if route is deleted
          if (!mapping.get(topicName).containsKey(topicRoute)) {
            log.info(
                "Delete topic route: direction='{}', topic='{}', type='{}', partition='{}'",
                topicRoute.getDirection(),
                topicRoute.getTopic(),
                topicRoute.getType(),
                topicRoute.getPartition()
            );
          }
        }
      }

      // delete topic session if last items was removed
      if (mapping.get(topicName).isEmpty()) {
        mapping.remove(topicName);
      }
    }
  }
}
