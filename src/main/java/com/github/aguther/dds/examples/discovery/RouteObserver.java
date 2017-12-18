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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.rti.dds.infrastructure.InstanceHandle_t;
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
    synchronized (mappingLock) {
      // create topic session if first item discovered
      if (!mapping.containsKey(data.topic_name)) {
        mapping.put(data.topic_name, ArrayListMultimap.create());
      }

      // create routes for all partitions we discovered
      if (data.partition.name.isEmpty()) {
        // create topic route object
        TopicRoute topicRoute = new TopicRoute(data.topic_name, data.type_name, "");

        // check if topic route is about to be created
        if (!mapping.get(data.topic_name).containsKey(topicRoute)) {
          log.info("Route is new");
        }

        // add instance handle to topic route
        mapping.get(data.topic_name).put(topicRoute, instanceHandle);
      } else {
        for (Object partition : data.partition.name) {
          // create topic route object
          TopicRoute topicRoute = new TopicRoute(data.topic_name, data.type_name, partition.toString());

          // check if topic route is about to be created
          if (!mapping.get(data.topic_name).containsKey(topicRoute)) {
            log.info("Route is new");
          }

          // add instance handle to topic route
          mapping.get(data.topic_name).put(topicRoute, instanceHandle);
        }
      }
    }
  }

  @Override
  public void publicationLost(
      InstanceHandle_t instanceHandle,
      PublicationBuiltinTopicData data
  ) {
    synchronized (mappingLock) {
      // delete routes for all partitions we lost
      if (data.partition.name.isEmpty()) {
        // create topic route object
        TopicRoute topicRoute = new TopicRoute(data.topic_name, data.type_name, "");

        // remove instance handle from topic route
        mapping.get(data.topic_name).put(topicRoute, instanceHandle);

        // check if route was deleted
        if (!mapping.get(data.topic_name).containsKey(topicRoute)) {
          log.info("Route is deleted");
        }
      } else {
        for (Object partition : data.partition.name) {
          // create topic route object
          TopicRoute topicRoute = new TopicRoute(data.topic_name, data.type_name, partition.toString());

          // remove instance handle from topic route
          mapping.get(data.topic_name).remove(topicRoute, instanceHandle);

          // check if route is deleted
          if (!mapping.get(data.topic_name).containsKey(topicRoute)) {
            log.info("Route is deleted");
          }
        }
      }

      // delete topic session if last items was removed
      if (mapping.get(data.topic_name).isEmpty()) {
        mapping.remove(data.topic_name);
      }
    }
  }

  @Override
  public void subscriptionDiscovered(
      InstanceHandle_t instanceHandle,
      SubscriptionBuiltinTopicData data
  ) {
    synchronized (mappingLock) {
    }
  }

  @Override
  public void subscriptionLost(
      InstanceHandle_t instanceHandle,
      SubscriptionBuiltinTopicData data
  ) {
    synchronized (mappingLock) {
    }
  }
}
