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

package io.github.aguther.dds.routing.dynamic.observer;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.StringSeq;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import io.github.aguther.dds.discovery.observer.PublicationObserverListener;
import io.github.aguther.dds.discovery.observer.SubscriptionObserverListener;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class implements a observer for publications and subscriptions.
 * <p>
 * When a publication or subscription is discovered, it determines if a session or topic route needs to be created and
 * invokes it's listeners accordingly.
 * <p>
 * This can be used to provide a function to dynamically route topics based on their partition without loosing their
 * origin (this happens when using asterisk or multiple partitions).
 */
public class DynamicPartitionObserver implements Closeable, PublicationObserverListener, SubscriptionObserverListener {

  private static final Logger LOGGER = LogManager.getLogger(DynamicPartitionObserver.class);

  private static final String DEFAULT_PARTITION = "";

  private final Map<Session, Multimap<TopicRoute, InstanceHandle_t>> mapping;
  private final Multimap<InstanceHandle_t, Session> mappingReverse;
  private final Set<DynamicPartitionObserverFilter> filters;
  private final Set<DynamicPartitionObserverListener> listeners;
  private final ExecutorService listenerExecutor;

  /**
   * Instantiates a new Dynamic partition observer.
   */
  public DynamicPartitionObserver() {
    mapping = Collections.synchronizedMap(new HashMap<>());
    mappingReverse = Multimaps.synchronizedMultimap(ArrayListMultimap.create());
    filters = Collections.synchronizedSet(new LinkedHashSet<>());
    listeners = Collections.synchronizedSet(new HashSet<>());
    listenerExecutor = Executors.newSingleThreadExecutor();
  }

  @Override
  public void close() {
    listeners.clear();
    listenerExecutor.shutdownNow();
  }

  /**
   * Add listener.
   *
   * @param listener the listener
   */
  public void addListener(
    final DynamicPartitionObserverListener listener
  ) {
    checkNotNull(listener, "Listener must not be null");
    listeners.add(listener);
  }

  /**
   * Remove listener.
   *
   * @param listener the listener
   */
  public void removeListener(
    final DynamicPartitionObserverListener listener
  ) {
    checkNotNull(listener, "Listener must not be null");
    listeners.remove(listener);
  }

  /**
   * Add filter.
   *
   * @param filter the filter
   */
  public void addFilter(
    final DynamicPartitionObserverFilter filter
  ) {
    checkNotNull(filter, "Filter must not be null");
    filters.add(filter);
  }

  /**
   * Remove filter.
   *
   * @param filter the filter
   */
  public void removeFilter(
    final DynamicPartitionObserverFilter filter
  ) {
    checkNotNull(filter, "Filter must not be null");
    filters.remove(filter);
  }

  @Override
  public void publicationDiscovered(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final PublicationBuiltinTopicData data
  ) {
    // ignore the publication?
    if (ignorePublication(domainParticipant, instanceHandle, data)) {
      return;
    }

    // handle discovered entity
    handleDiscovered(
      instanceHandle,
      Direction.OUT,
      data.topic_name,
      data.type_name,
      convertPartitionsToList(data.partition.name)
    );
  }

  @Override
  public void publicationModified(
    DomainParticipant domainParticipant,
    InstanceHandle_t instanceHandle,
    PublicationBuiltinTopicData data
  ) {
    // ignore the publication?
    if (ignorePublication(domainParticipant, instanceHandle, data)) {
      return;
    }

    // handle discovered entity
    handleModified(
      instanceHandle,
      Direction.OUT,
      data.topic_name,
      data.type_name,
      convertPartitionsToList(data.partition.name)
    );
  }

  @Override
  public void publicationLost(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final PublicationBuiltinTopicData data
  ) {
    // ignore the publication?
    if (ignorePublication(domainParticipant, instanceHandle, data)) {
      return;
    }

    // handle lost entity
    handleLost(
      instanceHandle,
      Direction.OUT,
      data.topic_name,
      data.type_name,
      convertPartitionsToList(data.partition.name)
    );
  }

  @Override
  public void subscriptionDiscovered(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final SubscriptionBuiltinTopicData data
  ) {
    // ignore the publication?
    if (ignoreSubscription(domainParticipant, instanceHandle, data)) {
      return;
    }

    // handle discovered entity
    handleDiscovered(
      instanceHandle,
      Direction.IN,
      data.topic_name,
      data.type_name,
      convertPartitionsToList(data.partition.name)
    );
  }

  @Override
  public void subscriptionModified(
    DomainParticipant domainParticipant,
    InstanceHandle_t instanceHandle,
    SubscriptionBuiltinTopicData data
  ) {
    // ignore the publication?
    if (ignoreSubscription(domainParticipant, instanceHandle, data)) {
      return;
    }

    // handle discovered entity
    handleModified(
      instanceHandle,
      Direction.IN,
      data.topic_name,
      data.type_name,
      convertPartitionsToList(data.partition.name)
    );
  }

  @Override
  public void subscriptionLost(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final SubscriptionBuiltinTopicData data
  ) {
    // ignore the publication?
    if (ignoreSubscription(domainParticipant, instanceHandle, data)) {
      return;
    }

    // handle lost entity
    handleLost(
      instanceHandle,
      Direction.IN,
      data.topic_name,
      data.type_name,
      convertPartitionsToList(data.partition.name)
    );
  }

  /**
   * Handles the discovery of a publication/subscription.
   *
   * @param instanceHandle instance handle for identification
   * @param direction      direction (OUT for publications, IN for subscriptions)
   * @param topicName      topic name
   * @param typeName       type name
   * @param partitions     partitions
   */
  private void handleDiscovered(
    final InstanceHandle_t instanceHandle,
    final Direction direction,
    final String topicName,
    final String typeName,
    final List<String> partitions
  ) {
    synchronized (mapping) {
      for (String partition : partitions) {
        // ignore partition?
        if (ignorePartition(topicName, partition)) {
          continue;
        }
        // add instance handle to map
        addInstanceHandleToMap(
          instanceHandle,
          new Session(direction, topicName, partition),
          new TopicRoute(direction, topicName, typeName)
        );
      }
    }
  }

  /**
   * Handles the modification of a publication/subscription.
   *
   * @param instanceHandle instance handle for identification
   * @param direction      direction (OUT for publications, IN for subscriptions)
   * @param topicName      topic name
   * @param typeName       type name
   * @param partitions     partitions
   */
  private void handleModified(
    final InstanceHandle_t instanceHandle,
    final Direction direction,
    final String topicName,
    final String typeName,
    final List<String> partitions
  ) {
    synchronized (mapping) {
      // remove routes for partitions that no longer exist
      for (Session session : ImmutableList.copyOf(mappingReverse.get(instanceHandle))) {
        // determine if partition of session is still active
        if (!partitions.contains(session.getPartition())) {
          // remove instance handles from map
          removeInstanceHandleFromMap(
            instanceHandle,
            session,
            new TopicRoute(direction, topicName, typeName)
          );
        }
      }
      // add routes for partitions that are new
      for (String partition : partitions) {
        // ignore partition?
        if (ignorePartition(topicName, partition)
          || mappingReverse.containsEntry(instanceHandle, new Session(direction, topicName, partition))) {
          continue;
        }
        // add instance handle to map
        addInstanceHandleToMap(
          instanceHandle,
          new Session(direction, topicName, partition),
          new TopicRoute(direction, topicName, typeName)
        );
      }
    }
  }

  /**
   * Handles the loss of a publication/subscription.
   *
   * @param instanceHandle instance handle for identification
   * @param direction      direction (OUT for publications, IN for subscriptions)
   * @param topicName      topic name
   * @param typeName       type name
   * @param partitions     partitions
   */
  private void handleLost(
    final InstanceHandle_t instanceHandle,
    final Direction direction,
    final String topicName,
    final String typeName,
    final List<String> partitions
  ) {
    synchronized (mapping) {
      // delete routes for all partitions we lost
      for (String partition : partitions) {
        // ignore partition?
        if (ignorePartition(topicName, partition)) {
          return;
        }
        // remove instance handle from map
        removeInstanceHandleFromMap(
          instanceHandle,
          new Session(direction, topicName, partition),
          new TopicRoute(direction, topicName, typeName)
        );
      }
    }
  }

  /**
   * Returns if a publication should be ignored using registered DynamicPartitionObserverFilter implementations.
   *
   * @param instanceHandle instance handle for identification
   * @param data           publication data
   * @return true if publication should be ignored, false if not
   */
  private boolean ignorePublication(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final PublicationBuiltinTopicData data
  ) {
    synchronized (filters) {
      for (DynamicPartitionObserverFilter filter : filters) {
        if (filter.ignorePublication(domainParticipant, instanceHandle, data)) {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
              "Ignoring publication topic='{}', type='{}', instance='{}' through filter '{}'",
              data.topic_name,
              data.type_name,
              instanceHandle,
              filter.getClass().getCanonicalName());
          }
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns if a subscriptions should be ignored using registered DynamicPartitionObserverFilter implementations.
   *
   * @param instanceHandle instance handle for identification
   * @param data           subscriptions data
   * @return true if subscriptions should be ignored, false if not
   */
  private boolean ignoreSubscription(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final SubscriptionBuiltinTopicData data
  ) {
    synchronized (filters) {
      for (DynamicPartitionObserverFilter filter : filters) {
        if (filter.ignoreSubscription(domainParticipant, instanceHandle, data)) {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
              "Ignoring subscription topic='{}', type='{}', instance='{}' through filter '{}'",
              data.topic_name,
              data.type_name,
              instanceHandle,
              filter.getClass().getCanonicalName());
          }
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns if a partition should be ignored using the registered DynamicPartitionObserverFilter implementations.
   *
   * @param topicName topic name
   * @param partition partition
   * @return true if publication should be ignored, false if not
   */
  private boolean ignorePartition(
    final String topicName,
    final String partition
  ) {
    synchronized (filters) {
      for (DynamicPartitionObserverFilter filter : filters) {
        if (filter.ignorePartition(topicName, partition)) {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
              "Ignoring partition topic='{}', name='{}' through filter '{}'",
              topicName,
              partition,
              filter.getClass().getCanonicalName());
          }
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Adds a instance handle to the mapping, triggers creation of sessions and routes if needed.
   *
   * @param instanceHandle instance handle for identification
   * @param session        session
   * @param topicRoute     topic route
   */
  private void addInstanceHandleToMap(
    final InstanceHandle_t instanceHandle,
    final Session session,
    final TopicRoute topicRoute
  ) {
    // create topic session if first item discovered
    if (!mapping.containsKey(session)) {
      mapping.put(session, ArrayListMultimap.create());
      mappingReverse.put(instanceHandle, session);
      createSession(session);
    }

    // check if topic route is about to be created
    if (!mapping.get(session).containsKey(topicRoute)) {
      createTopicRoute(session, topicRoute);
    }

    // add instance handle to topic route
    if (!mapping.get(session).get(topicRoute).contains(instanceHandle)) {
      mapping.get(session).put(topicRoute, instanceHandle);
    }
  }

  /**
   * Removes a instance handle from the mapping, triggers deletion of sessions and routes if needed.
   *
   * @param instanceHandle instance handle for identification
   * @param session        session
   * @param topicRoute     topic route
   */
  private void removeInstanceHandleFromMap(
    final InstanceHandle_t instanceHandle,
    final Session session,
    final TopicRoute topicRoute
  ) {
    // ensure session and topic route are existing
    // otherwise we have nothing do to
    if (!mapping.containsKey(session)
      || !mapping.get(session).containsKey(topicRoute)) {
      return;
    }

    // remove instance handle from topic route
    mapping.get(session).remove(topicRoute, instanceHandle);

    // check if route was deleted
    if (!mapping.get(session).containsKey(topicRoute)) {
      deleteTopicRoute(session, topicRoute);
    }

    // delete topic session if last items was removed
    if (mapping.get(session).isEmpty()) {
      mapping.remove(session);
      mappingReverse.remove(instanceHandle, session);
      deleteSession(session);
    }
  }

  /**
   * Triggers the creation of a session by invoking the corresponding listener interface.
   *
   * @param session session that should be created
   */
  private void createSession(
    final Session session
  ) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
        "Calling 'createSession' on listeners with topic='{}', partition='{}'",
        session.getTopic(),
        session.getPartition()
      );
    }
    // invoke listener
    listenerExecutor.submit(() -> {
      synchronized (listeners) {
        for (DynamicPartitionObserverListener listener : listeners) {
          listener.createSession(session);
        }
      }
    });
  }

  /**
   * Triggers the deletion of a session by invoking the corresponding listener interface.
   *
   * @param session session that should be deleted
   */
  private void deleteSession(
    final Session session
  ) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
        "Calling 'deleteSession' on listeners with topic='{}', partition='{}'",
        session.getTopic(),
        session.getPartition()
      );
    }
    // invoke listener
    listenerExecutor.submit(() -> {
      synchronized (listeners) {
        for (DynamicPartitionObserverListener listener : listeners) {
          listener.deleteSession(session);
        }
      }
    });
  }

  /**
   * Triggers the creation of a topic route by invoking the corresponding listener interface.
   *
   * @param session    session belonging to the topic route
   * @param topicRoute topic route that should be created
   */
  private void createTopicRoute(
    final Session session,
    final TopicRoute topicRoute
  ) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
        "Calling 'createTopicRoute' on listeners with topic='{}', type='{}', partition='{}', direction='{}'",
        session.getTopic(),
        topicRoute.getType(),
        session.getPartition(),
        topicRoute.getDirection()
      );
    }
    // invoke listener
    listenerExecutor.submit(() -> {
      synchronized (listeners) {
        for (DynamicPartitionObserverListener listener : listeners) {
          listener.createTopicRoute(session, topicRoute);
        }
      }
    });
  }

  /**
   * Triggers the deletion of a topic route by invoking the corresponding listener interface.
   *
   * @param session    session belonging to the topic route
   * @param topicRoute topic route that should be deleted
   */
  private void deleteTopicRoute(
    final Session session,
    final TopicRoute topicRoute
  ) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
        "Calling 'deleteTopicRoute' on listeners with topic='{}', type='{}', partition='{}', direction='{}'",
        session.getTopic(),
        topicRoute.getType(),
        session.getPartition(),
        topicRoute.getDirection()
      );
    }
    // invoke listener
    listenerExecutor.submit(() -> {
      synchronized (listeners) {
        for (DynamicPartitionObserverListener listener : listeners) {
          listener.deleteTopicRoute(session, topicRoute);
        }
      }
    });
  }

  /**
   * Convert the sequence with partitions into a list with partitions. In case the sequence is empty, add the default
   * partition which equals to ''.
   *
   * @param stringSeq String sequence with partitions
   * @return List of partitions
   */
  private List<String> convertPartitionsToList(
    StringSeq stringSeq
  ) {
    // check argument
    checkNotNull(stringSeq, "String sequence must not be null!");

    // create list with partitions
    List<String> list = new ArrayList<>(stringSeq.size());

    // when partition is empty add default partition, otherwise copy items
    if (stringSeq.isEmpty()) {
      list.add(DEFAULT_PARTITION);
    } else {
      for (Object item : stringSeq) {
        list.add((String) item);
      }
    }

    // return result
    return list;
  }
}
