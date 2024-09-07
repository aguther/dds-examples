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

package io.github.aguther.dds.routing.dynamic.observer.filter;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.builtin.ParticipantBuiltinTopicData;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.PropertyQosPolicyHelper;
import com.rti.dds.infrastructure.Property_t;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import com.rti.dds.topic.BuiltinTopicKey_t;
import io.github.aguther.dds.routing.dynamic.observer.DynamicPartitionObserverFilter;
import io.github.aguther.dds.util.BuiltinTopicHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Filter to ignore entities that belong to routing services of a given group.
 */
public class RoutingServiceGroupEntitiesFilter implements DynamicPartitionObserverFilter {

  private static final Logger LOGGER = LogManager.getLogger(RoutingServiceGroupEntitiesFilter.class);

  private final String groupName;

  public RoutingServiceGroupEntitiesFilter(
    final String groupName
  ) {
    this.groupName = groupName;
  }

  @Override
  public boolean ignorePublication(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final PublicationBuiltinTopicData data
  ) {
    return isRoutingServiceGroupEntity(
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
    return isRoutingServiceGroupEntity(
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

  private boolean isRoutingServiceGroupEntity(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final BuiltinTopicKey_t participantKey
  ) {
    // get data of parent domain participant
    ParticipantBuiltinTopicData participantData = BuiltinTopicHelper.getParticipantBuiltinTopicData(
      domainParticipant,
      participantKey
    );

    if (participantData != null) {
      // get group name of routing service
      Property_t property = PropertyQosPolicyHelper.lookup_property(
        participantData.property,
        "rti.routing_service.group_name"
      );

      // when participant is part of routing service group ignore it
      boolean result = (property != null && (property.value.equals(groupName)));

      // log decision
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(
          "instance='{}', ignore='{}' (filter='{}', group_name='{}')",
          instanceHandle,
          result,
          groupName,
          property != null ? property.value : "none"
        );
      }

      // return result
      return result;
    }

    // log decision
    LOGGER.trace(
      "instance='{}', ignore='{}' (participant data not found)",
      instanceHandle,
      "false"
    );

    // do not ignore
    return false;
  }
}
