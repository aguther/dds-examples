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

package io.github.aguther.dds.discovery.observer;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;

/**
 * Callback interface to get notified when a subscription is discovered or lost.
 */
public interface SubscriptionObserverListener {

  /**
   * Invoked when a new subscription has been discovered.
   *
   * @param domainParticipant domain participant used for discovery
   * @param instanceHandle    instance handle of subscription for identification
   * @param data              subscription data
   */
  void subscriptionDiscovered(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final SubscriptionBuiltinTopicData data
  );

  /**
   * Invoked when a subscription has been modified (e.g. partitions changed).
   *
   * @param domainParticipant domain participant used for discovery
   * @param instanceHandle    instance handle of publication for identification
   * @param data              publication data
   */
  void subscriptionModified(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final SubscriptionBuiltinTopicData data
  );

  /**
   * Invoked when a subscription has been lost.
   *
   * @param domainParticipant domain participant used for discovery
   * @param instanceHandle    instance handle of subscription for identification
   * @param data              subscription data
   */
  void subscriptionLost(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final SubscriptionBuiltinTopicData data
  );
}
