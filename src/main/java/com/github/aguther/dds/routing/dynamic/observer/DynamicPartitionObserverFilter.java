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

package com.github.aguther.dds.routing.dynamic.observer;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;

/**
 * Callback interface to get determine if a entity should be ignored.
 *
 * This can be used to e.g. ignore routing service entities or wildcard partitions.
 */
public interface DynamicPartitionObserverFilter {

  /**
   * Invoked to determine if a publication should be ignored.
   *
   * @param domainParticipant domain participant for reference
   * @param instanceHandle instance handle for identification
   * @param data publication data
   * @return true to ignore publication, false to allow
   */
  boolean ignorePublication(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final PublicationBuiltinTopicData data
  );

  /**
   * Invoked to determine if a subscription should be ignored.
   *
   * @param domainParticipant domain participant for reference
   * @param instanceHandle instance handle for identification
   * @param data subscription data
   * @return true to ignore publication, false to allow
   */
  boolean ignoreSubscription(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final SubscriptionBuiltinTopicData data
  );

  /**
   * Invoked to determine if a partition should be ignored.
   *
   * @param topicName topic name of partition
   * @param partition partition to check
   * @return true to ignore partition, false to allow
   */
  boolean ignorePartition(
    final String topicName,
    final String partition
  );
}
