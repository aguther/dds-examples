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

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantFactoryQos;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.PartitionQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discovery implements PublicationObserverListener, SubscriptionObserverListener {

  private static final Logger log;

  private static boolean shouldTerminate;

  static {
    log = LoggerFactory.getLogger(Discovery.class);
  }

  public static void main(String[] args) throws InterruptedException {

    // create an instance
    Discovery discovery = new Discovery();

    // register shutdown hook
    registerShutdownHook();

    // do not auto-enable entities to ensure we do not miss any discovery data
    DomainParticipantFactoryQos domainParticipantFactoryQos = new DomainParticipantFactoryQos();
    DomainParticipantFactory.get_instance().get_qos(domainParticipantFactoryQos);
    domainParticipantFactoryQos.entity_factory.autoenable_created_entities = false;
    DomainParticipantFactory.get_instance().set_qos(domainParticipantFactoryQos);

    // create domain participant
    DomainParticipant domainParticipant = DomainParticipantFactory.get_instance().create_participant(
        0,
        DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
        null,
        StatusKind.STATUS_MASK_NONE);

    // create new publication observer
    PublicationObserver publicationObserver = new PublicationObserver(domainParticipant);
    publicationObserver.addListener(discovery);
    // create new subscription observer
    SubscriptionObserver subscriptionObserver = new SubscriptionObserver(domainParticipant);
    subscriptionObserver.addListener(discovery);

    // enable domain participant
    domainParticipant.enable();

    while (!shouldTerminate) {
      Thread.sleep(1000);
    }

    // close observers
    publicationObserver.close();
    subscriptionObserver.close();

    // shutdown DDS
    DomainParticipantFactory.get_instance().delete_participant(domainParticipant);
    DomainParticipantFactory.finalize_instance();
  }

  private static void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Shutdown signal received...");
      shouldTerminate = true;
    }));
  }

  private String InstanceHandleToString(
      InstanceHandle_t instanceHandle
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

  private String PartitionQosPolicyToString(
      PartitionQosPolicy partitionQosPolicy
  ) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append('[');
    {
      for (int i = 0; i < partitionQosPolicy.name.getMaximum(); ++i) {
        stringBuilder.append(partitionQosPolicy.name.get(i));
        if (i < (partitionQosPolicy.name.getMaximum() - 1)) {
          stringBuilder.append(",");
        }
      }
    }
    stringBuilder.append(']');

    return stringBuilder.toString();
  }

  @Override
  public void publicationDiscovered(
      InstanceHandle_t instanceHandle,
      PublicationBuiltinTopicData data
  ) {
    log.info(
        "Discovered Publication : instance='{}', topic='{}', type='{}', partitions='{}'",
        InstanceHandleToString(instanceHandle),
        data.topic_name,
        data.type_name,
        PartitionQosPolicyToString(data.partition)
    );
  }

  @Override
  public void publicationLost(
      InstanceHandle_t instanceHandle,
      PublicationBuiltinTopicData data
  ) {
    log.info(
        "Lost Publication       : instance='{}', topic='{}', type='{}', partitions='{}'",
        InstanceHandleToString(instanceHandle),
        data.topic_name,
        data.type_name,
        PartitionQosPolicyToString(data.partition)
    );
  }

  @Override
  public void subscriptionDiscovered(
      InstanceHandle_t instanceHandle,
      SubscriptionBuiltinTopicData data
  ) {
    log.info(
        "Discovered Subscription: instance='{}', topic='{}', type='{}', partitions='{}'",
        InstanceHandleToString(instanceHandle),
        data.topic_name,
        data.type_name,
        PartitionQosPolicyToString(data.partition)
    );
  }

  @Override
  public void subscriptionLost(
      InstanceHandle_t instanceHandle,
      SubscriptionBuiltinTopicData data
  ) {
    log.info(
        "Lost Subscription      : instance='{}', topic='{}', type='{}', partitions='{}'",
        InstanceHandleToString(instanceHandle),
        data.topic_name,
        data.type_name,
        PartitionQosPolicyToString(data.partition)
    );
  }
}
