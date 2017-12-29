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

import com.github.aguther.dds.discovery.observer.PublicationObserver;
import com.github.aguther.dds.discovery.observer.PublicationObserverListener;
import com.github.aguther.dds.discovery.observer.SubscriptionObserver;
import com.github.aguther.dds.discovery.observer.SubscriptionObserverListener;
import com.github.aguther.dds.util.AutoEnableCreatedEntitiesHelper;
import com.google.common.util.concurrent.AbstractIdleService;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.PartitionQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discovery extends AbstractIdleService
    implements PublicationObserverListener, SubscriptionObserverListener {

  private static final Logger log = LoggerFactory.getLogger(Discovery.class);

  private static Discovery serviceInstance;

  private DomainParticipant domainParticipant;
  private PublicationObserver publicationObserver;
  private SubscriptionObserver subscriptionObserver;

  public static void main(
      final String[] args
  ) {
    // register shutdown hook
    registerShutdownHook();

    // create service
    serviceInstance = new Discovery();

    // start the service
    serviceInstance.startAsync();

    // wait for termination
    serviceInstance.awaitTerminated();

    // service terminated
    log.info("Service terminated");
  }

  private static void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Shutdown signal received");
      if (serviceInstance != null) {
        serviceInstance.stopAsync();
        serviceInstance.awaitTerminated();
      }
      log.info("Shutdown signal finished");
    }));
  }

  @Override
  protected void startUp() {
    // log service start
    log.info("Service is starting");

    // do not auto-enable entities to ensure we do not miss any discovery data
    AutoEnableCreatedEntitiesHelper.disable();

    // create domain participant for discovery
    domainParticipant = DomainParticipantFactory.get_instance().create_participant(
        0,
        DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
        null,
        StatusKind.STATUS_MASK_NONE);

    // create new publication observer
    publicationObserver = new PublicationObserver(domainParticipant);
    subscriptionObserver = new SubscriptionObserver(domainParticipant);

    // add listeners
    publicationObserver.addListener(this);
    subscriptionObserver.addListener(this);

    // enable domain participant
    domainParticipant.enable();

    // log service start
    log.info("Service start finished");
  }

  @Override
  protected void shutDown() {
    // log service start
    log.info("Service is shutting down");

    // shutdown observers
    if (publicationObserver != null) {
      publicationObserver.close();
    }
    if (subscriptionObserver != null) {
      subscriptionObserver.close();
    }

    // shutdown DDS
    if (domainParticipant != null) {
      domainParticipant.delete_contained_entities();
      DomainParticipantFactory.get_instance().delete_participant(domainParticipant);
    }
    DomainParticipantFactory.finalize_instance();

    // log service start
    log.info("Service shutdown finished");
  }

  private String convertInstanceHandleToString(
      final InstanceHandle_t instanceHandle
  ) {
    StringBuilder stringBuilder = new StringBuilder();
    byte[] values = instanceHandle.get_valuesI();
    for (int i = 0; i < values.length; ++i) {
      stringBuilder.append(String.format("%3d", Byte.toUnsignedInt(values[i])));
      if (i < (values.length - 1)) {
        stringBuilder.append(",");
      }
    }

    return stringBuilder.toString();
  }

  private String convertPartitionQosPolicyToString(
      final PartitionQosPolicy partitionQosPolicy
  ) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append('[');

    for (int i = 0; i < partitionQosPolicy.name.getMaximum(); ++i) {
      stringBuilder.append(partitionQosPolicy.name.get(i));
      if (i < (partitionQosPolicy.name.getMaximum() - 1)) {
        stringBuilder.append(",");
      }
    }

    stringBuilder.append(']');

    return stringBuilder.toString();
  }

  @Override
  public void publicationDiscovered(
      final DomainParticipant domainParticipant,
      final InstanceHandle_t instanceHandle,
      final PublicationBuiltinTopicData data
  ) {
    if (log.isInfoEnabled()) {
      log.info(
          "Discovered Publication : instance='{}', topic='{}', type='{}', partitions='{}'",
          convertInstanceHandleToString(instanceHandle),
          data.topic_name,
          data.type_name,
          convertPartitionQosPolicyToString(data.partition)
      );
    }
  }

  @Override
  public void publicationLost(
      final DomainParticipant domainParticipant,
      final InstanceHandle_t instanceHandle,
      final PublicationBuiltinTopicData data
  ) {
    if (log.isInfoEnabled()) {
      log.info(
          "Lost Publication       : instance='{}', topic='{}', type='{}', partitions='{}'",
          convertInstanceHandleToString(instanceHandle),
          data.topic_name,
          data.type_name,
          convertPartitionQosPolicyToString(data.partition)
      );
    }
  }

  @Override
  public void subscriptionDiscovered(
      final DomainParticipant domainParticipant,
      final InstanceHandle_t instanceHandle,
      final SubscriptionBuiltinTopicData data
  ) {
    if (log.isInfoEnabled()) {
      log.info(
          "Discovered Subscription: instance='{}', topic='{}', type='{}', partitions='{}'",
          convertInstanceHandleToString(instanceHandle),
          data.topic_name,
          data.type_name,
          convertPartitionQosPolicyToString(data.partition)
      );
    }
  }

  @Override
  public void subscriptionLost(
      final DomainParticipant domainParticipant,
      final InstanceHandle_t instanceHandle,
      final SubscriptionBuiltinTopicData data
  ) {
    if (log.isInfoEnabled()) {
      log.info(
          "Lost Subscription      : instance='{}', topic='{}', type='{}', partitions='{}'",
          convertInstanceHandleToString(instanceHandle),
          data.topic_name,
          data.type_name,
          convertPartitionQosPolicyToString(data.partition)
      );
    }
  }
}
