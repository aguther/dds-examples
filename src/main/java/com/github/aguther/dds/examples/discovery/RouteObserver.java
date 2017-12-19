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
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.StringSeq;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteObserver implements PublicationObserverListener, SubscriptionObserverListener {

  private static final Logger log;

  static {
    log = LoggerFactory.getLogger(RouteObserver.class);
  }

  private final DomainParticipant domainParticipant;

  private final Object mappingLock;
  private final HashMap<Session, Multimap<TopicRoute, InstanceHandle_t>> mapping;

  private final Object filterLock;
  private final List<RouteObserverFilter> filterList;

  private final Object listenerLock;
  private final List<RouteObserverListener> listenerList;
  private final ExecutorService listenerExecutor;

  public RouteObserver(
      DomainParticipant domainParticipant
  ) {
    this.domainParticipant = domainParticipant;

    mapping = new HashMap<>();
    mappingLock = new Object();

    filterLock = new Object();
    filterList = new ArrayList<>();

    listenerLock = new Object();
    listenerList = new ArrayList<>();
    listenerExecutor = Executors.newSingleThreadExecutor();
  }

  public void addListener(
      RouteObserverListener listener
  ) {
    synchronized (listenerLock) {
      if (!listenerList.contains(listener)) {
        listenerList.add(listener);
      }
    }
  }

  public void removeListener(
      RouteObserverListener listener
  ) {
    synchronized (listenerLock) {
      listenerList.remove(listener);
    }
  }

  public void addFilter(
      RouteObserverFilter filter
  ) {
    synchronized (filterLock) {
      if (!filterList.contains(filter)) {
        filterList.add(filter);
      }
    }
  }

  public void removeFilter(
      RouteObserverFilter filter
  ) {
    synchronized (filterLock) {
      filterList.remove(filter);
    }
  }

  @Override
  public void publicationDiscovered(
      InstanceHandle_t instanceHandle,
      PublicationBuiltinTopicData data
  ) {
    // ignore the publication?
    synchronized (filterLock) {
      for (RouteObserverFilter filter : filterList) {
        if (filter.ignorePublication(domainParticipant, instanceHandle, data)) {
          return;
        }
      }
    }

    // handle discovered entity
    handleDiscovered(
        instanceHandle,
        Direction.OUT,
        data.topic_name,
        data.partition.name
    );
  }

  @Override
  public void publicationLost(
      InstanceHandle_t instanceHandle,
      PublicationBuiltinTopicData data
  ) {
    // ignore the publication?
    synchronized (filterLock) {
      for (RouteObserverFilter filter : filterList) {
        if (filter.ignorePublication(domainParticipant, instanceHandle, data)) {
          return;
        }
      }
    }

    // handle lost entity
    handleLost(
        instanceHandle,
        Direction.OUT,
        data.topic_name,
        data.partition.name
    );
  }

  @Override
  public void subscriptionDiscovered(
      InstanceHandle_t instanceHandle,
      SubscriptionBuiltinTopicData data
  ) {
    // ignore the publication?
    synchronized (filterLock) {
      for (RouteObserverFilter filter : filterList) {
        if (filter.ignoreSubscription(domainParticipant, instanceHandle, data)) {
          return;
        }
      }
    }

    // handle discovered entity
    handleDiscovered(
        instanceHandle,
        Direction.IN,
        data.topic_name,
        data.partition.name
    );
  }

  @Override
  public void subscriptionLost(
      InstanceHandle_t instanceHandle,
      SubscriptionBuiltinTopicData data
  ) {
    // ignore the publication?
    synchronized (filterLock) {
      for (RouteObserverFilter filter : filterList) {
        if (filter.ignoreSubscription(domainParticipant, instanceHandle, data)) {
          return;
        }
      }
    }

    // handle lost entity
    handleLost(
        instanceHandle,
        Direction.IN,
        data.topic_name,
        data.partition.name
    );
  }

  private void handleDiscovered(
      InstanceHandle_t instanceHandle,
      Direction direction,
      String topicName,
      StringSeq partitions
  ) {
    synchronized (mappingLock) {
      // create routes for all partitions we discovered
      if (partitions.isEmpty()) {
        // add instance handle to map
        addInstanceHandleToMap(
            instanceHandle,
            new Session(topicName, ""),
            new TopicRoute(direction, topicName)
        );
      } else {
        for (Object partition : partitions) {
          // add instance handle to map
          addInstanceHandleToMap(
              instanceHandle,
              new Session(topicName, partition.toString()),
              new TopicRoute(direction, topicName)
          );
        }
      }
    }
  }

  private void handleLost(
      InstanceHandle_t instanceHandle,
      Direction direction,
      String topicName,
      StringSeq partitions
  ) {
    synchronized (mappingLock) {
      // delete routes for all partitions we lost
      if (partitions.isEmpty()) {
        // remove instance handle from map
        removeInstanceHandleFromMap(
            instanceHandle,
            new Session(topicName, ""),
            new TopicRoute(direction, topicName)
        );
      } else {
        for (Object partition : partitions) {
          // remove instance handle from map
          removeInstanceHandleFromMap(
              instanceHandle,
              new Session(topicName, partition.toString()),
              new TopicRoute(direction, topicName)
          );
        }
      }
    }
  }

  private void addInstanceHandleToMap(
      InstanceHandle_t instanceHandle,
      Session session,
      TopicRoute topicRoute
  ) {
    // create topic session if first item discovered
    if (!mapping.containsKey(session)) {
      mapping.put(session, ArrayListMultimap.create());
      createSession(session);
    }

    // check if topic route is about to be created
    if (!mapping.get(session).containsKey(topicRoute)) {
      createTopicRoute(session, topicRoute);
    }

    // add instance handle to topic route
    mapping.get(session).put(topicRoute, instanceHandle);
  }

  private void removeInstanceHandleFromMap(
      InstanceHandle_t instanceHandle,
      Session session,
      TopicRoute topicRoute
  ) {
    // remove instance handle from topic route
    mapping.get(session).remove(topicRoute, instanceHandle);

    // check if route was deleted
    if (!mapping.get(session).containsKey(topicRoute)) {
      deleteTopicRoute(session, topicRoute);
    }

    // delete topic session if last items was removed
    if (mapping.get(session).isEmpty()) {
      mapping.remove(session);
      deleteSession(session);
    }
  }

  private void createSession(
      Session session
  ) {
    // invoke listener
    listenerExecutor.submit(() -> {
      synchronized (listenerLock) {
        for (RouteObserverListener listener : listenerList) {
          listener.createSession(session);
        }
      }
    });
  }

  private void deleteSession(
      Session session
  ) {
    // invoke listener
    listenerExecutor.submit(() -> {
      synchronized (listenerLock) {
        for (RouteObserverListener listener : listenerList) {
          listener.deleteSession(session);
        }
      }
    });
  }

  private void createTopicRoute(
      Session session,
      TopicRoute topicRoute
  ) {
    // invoke listener
    listenerExecutor.submit(() -> {
      synchronized (listenerLock) {
        for (RouteObserverListener listener : listenerList) {
          listener.createTopicRoute(session, topicRoute);
        }
      }
    });
  }

  private void deleteTopicRoute(
      Session session,
      TopicRoute topicRoute
  ) {
    // invoke listener
    listenerExecutor.submit(() -> {
      synchronized (listenerLock) {
        for (RouteObserverListener listener : listenerList) {
          listener.deleteTopicRoute(session, topicRoute);
        }
      }
    });
  }
}
