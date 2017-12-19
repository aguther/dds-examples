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

package com.github.aguther.dds.examples.routing;

import com.github.aguther.dds.examples.routing.dynamic.observer.DynamicPartitionObserverFilter;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.builtin.ParticipantBuiltinTopicData;
import com.rti.dds.infrastructure.InstanceHandleSeq;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.PropertyQosPolicyHelper;
import com.rti.dds.infrastructure.Property_t;
import com.rti.dds.infrastructure.ServiceQosPolicyKind;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import com.rti.dds.topic.BuiltinTopicKey_t;

public class IgnoreRtiTopicsAndRoutingServiceEntitiesFilter implements DynamicPartitionObserverFilter {

  private final String groupName;

  public IgnoreRtiTopicsAndRoutingServiceEntitiesFilter(
      String groupName
  ) {
    this.groupName = groupName;
  }

  @Override
  public boolean ignorePublication(
      DomainParticipant domainParticipant,
      InstanceHandle_t instanceHandle,
      PublicationBuiltinTopicData data
  ) {
    return ignore(domainParticipant, data.topic_name, data.participant_key);
  }

  @Override
  public boolean ignoreSubscription(
      DomainParticipant domainParticipant,
      InstanceHandle_t instanceHandle,
      SubscriptionBuiltinTopicData data
  ) {
    return ignore(domainParticipant, data.topic_name, data.participant_key);
  }

  private boolean ignore(
      DomainParticipant domainParticipant,
      String topicName,
      BuiltinTopicKey_t participantKey
  ) {
    // ignore all rti internal topics
    if (topicName.startsWith("rti")) {
      return true;
    }

    // get data of parent domain participant
    ParticipantBuiltinTopicData participantData = getParticipantBuiltinTopicData(
        domainParticipant, participantKey);

    // check if participant belongs to a routing service
    if (participantData != null) {
      // check if participant belongs to a routing service
      if (participantData.service.kind != ServiceQosPolicyKind.ROUTING_SERVICE_QOS) {
        return false;
      }

      // get group name of routing service
      Property_t property = PropertyQosPolicyHelper.lookup_property(
          participantData.property,
          "rti.routing_service.group_name"
      );

      // when participant is part of routing service group ignore it
      return (property != null && (property.value.equals(groupName)));
    }

    // do not ignore
    return false;
  }

  private ParticipantBuiltinTopicData getParticipantBuiltinTopicData(
      DomainParticipant domainParticipant,
      BuiltinTopicKey_t participantKey
  ) {
    // get discovered participants
    InstanceHandleSeq participantHandles = new InstanceHandleSeq();
    domainParticipant.get_discovered_participants(participantHandles);

    // iterate over handles
    ParticipantBuiltinTopicData participantData = new ParticipantBuiltinTopicData();
    for (Object participantHandle : participantHandles) {
      domainParticipant.get_discovered_participant_data(
          participantData,
          (InstanceHandle_t) participantHandle
      );

      if (participantData.key.equals(participantKey)) {
        return participantData;
      }
    }

    // nothing found
    return null;
  }
}
