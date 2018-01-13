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

package com.github.aguther.dds.routing.dynamic.observer.filter;

import com.github.aguther.dds.routing.dynamic.observer.DynamicPartitionObserverFilter;
import com.github.aguther.dds.util.BuiltinTopicHelper;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.builtin.ParticipantBuiltinTopicData;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.ServiceQosPolicyKind;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import com.rti.dds.topic.BuiltinTopicKey_t;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter to ignore entities that belong to any routing service.
 */
public class RoutingServiceEntitiesFilter implements DynamicPartitionObserverFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RoutingServiceEntitiesFilter.class);

  @Override
  public boolean ignorePublication(
      final DomainParticipant domainParticipant,
      final InstanceHandle_t instanceHandle,
      final PublicationBuiltinTopicData data
  ) {
    return isRoutingServiceEntity(
        domainParticipant,
        instanceHandle,
        data.participant_key
    );
  }

  @Override
  public boolean ignoreSubscription(
      final DomainParticipant domainParticipant,
      final InstanceHandle_t instanceHandle,
      final SubscriptionBuiltinTopicData data
  ) {
    return isRoutingServiceEntity(
        domainParticipant,
        instanceHandle,
        data.participant_key
    );
  }

  @Override
  public boolean ignorePartition(
      final String topicName,
      final String partition
  ) {
    return false;
  }

  private boolean isRoutingServiceEntity(
      final DomainParticipant domainParticipant,
      final InstanceHandle_t instanceHandle,
      final BuiltinTopicKey_t participantKey
  ) {
    // get data of parent domain participant
    ParticipantBuiltinTopicData participantData = BuiltinTopicHelper.getParticipantBuiltinTopicData(
        domainParticipant,
        participantKey
    );

    // check if participant belongs to a routing service
    boolean result = (participantData != null
        && participantData.service.kind == ServiceQosPolicyKind.ROUTING_SERVICE_QOS);

    // log decision
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace(
          "instance='{}', ignore='{}' (filter='{}', service.kind='{}')",
          instanceHandle,
          result,
          ServiceQosPolicyKind.ROUTING_SERVICE_QOS.toString(),
          participantData != null ? participantData.service.kind.toString() : "unknown"
      );
    }

    // return result
    return result;
  }
}
