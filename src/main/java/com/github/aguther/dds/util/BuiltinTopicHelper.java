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
import com.rti.dds.infrastructure.RETCODE_PRECONDITION_NOT_MET;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import com.rti.dds.topic.BuiltinTopicKey_t;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuiltinTopicHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuiltinTopicHelper.class);

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
  public static synchronized ParticipantBuiltinTopicData getParticipantBuiltinTopicData(
      final DomainParticipant domainParticipant,
      final BuiltinTopicKey_t participantKey
  ) {
    // get discovered participants
    InstanceHandleSeq participantHandles = new InstanceHandleSeq();
    domainParticipant.get_discovered_participants(participantHandles);

    // iterate over handles
    ParticipantBuiltinTopicData participantData = new ParticipantBuiltinTopicData();
    for (Object participantHandle : participantHandles) {
      try {
        domainParticipant.get_discovered_participant_data(
            participantData,
            (InstanceHandle_t) participantHandle
        );
      } catch (RETCODE_PRECONDITION_NOT_MET ex) {
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info(
              "Ignoring participant ({}) that got lost between call get_discovered_participants() and get_discovered_participant_data()",
              participantHandle == null ? "null" : participantHandle
          );
        }
        // when during call to get_discovered_participants and this loop a participant is lost,
        // it might return PRECONDITION_NOT_MET because this participant handle will no
        // longer be valid -> ignore this participant and check the other handles
        continue;
      }

      if (participantData.key.equals(participantKey)) {
        return participantData;
      }
    }

    // nothing found
    return null;
  }

  public static String toString(
      final int[] key
  ) {
    return String.format(
        "%08x.%08x.%08x.%08x",
        key[0],
        key[1],
        key[2],
        key[3]
    );
  }

  public static String toString(
      final byte[] key
  ) {
    return String.format(
        "%02x.%02x.%02x.%02x",
        key[0],
        key[1],
        key[2],
        key[3]
    );
  }
}
