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

package com.github.aguther.dds.util;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.builtin.ParticipantBuiltinTopicData;
import com.rti.dds.infrastructure.InstanceHandleSeq;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import com.rti.dds.topic.BuiltinTopicKey_t;

public class BuiltinTopicHelper {

  private BuiltinTopicHelper() {
  }

  /**
   * Gets participant builtin topic data from publication.
   *
   * @param domainParticipant the domain participant
   * @param publicationBuiltinTopicData the publication builtin topic data
   * @return the participant builtin topic data from publication
   */
  public static ParticipantBuiltinTopicData getParticipantBuiltinTopicData(
      final DomainParticipant domainParticipant,
      final PublicationBuiltinTopicData publicationBuiltinTopicData
  ) {
    return getParticipantBuiltinTopicData(
        domainParticipant,
        publicationBuiltinTopicData.participant_key
    );
  }

  /**
   * Gets participant builtin topic data from subscription.
   *
   * @param domainParticipant the domain participant
   * @param subscriptionBuiltinTopicData the subscription builtin topic data
   * @return the participant builtin topic data from subscription
   */
  public static ParticipantBuiltinTopicData getParticipantBuiltinTopicData(
      final DomainParticipant domainParticipant,
      final SubscriptionBuiltinTopicData subscriptionBuiltinTopicData
  ) {
    return getParticipantBuiltinTopicData(
        domainParticipant,
        subscriptionBuiltinTopicData.participant_key);
  }

  /**
   * Gets participant builtin topic data from a participant key.
   *
   * @param domainParticipant the domain participant
   * @param participantKey the participant key
   * @return the participant builtin topic data from participant key
   */
  public static ParticipantBuiltinTopicData getParticipantBuiltinTopicData(
      final DomainParticipant domainParticipant,
      final BuiltinTopicKey_t participantKey
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
