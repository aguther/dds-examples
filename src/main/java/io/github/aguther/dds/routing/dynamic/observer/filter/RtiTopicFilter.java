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
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import io.github.aguther.dds.routing.dynamic.observer.DynamicPartitionObserverFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Filter to ignore all RTI topics like routing service administration.
 */
public class RtiTopicFilter implements DynamicPartitionObserverFilter {

  private static final Logger LOGGER = LogManager.getLogger(RtiTopicFilter.class);

  @Override
  public boolean ignorePublication(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final PublicationBuiltinTopicData data
  ) {
    return isRtiTopic(
      instanceHandle,
      data.topic_name
    );
  }

  @Override
  public boolean ignoreSubscription(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final SubscriptionBuiltinTopicData data
  ) {
    return isRtiTopic(
      instanceHandle,
      data.topic_name
    );
  }

  @Override
  public boolean ignorePartition(
    final String topicName,
    final String partition
  ) {
    return false;
  }

  private boolean isRtiTopic(
    final InstanceHandle_t instanceHandle,
    final String topicName
  ) {
    // ignore all rti topics
    boolean result = topicName.startsWith("rti");

    // log decision
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace(
        "instance='{}', ignore='{}' (filter='{}', topic='{}')",
        instanceHandle,
        result,
        "startsWith(\"rti\")",
        topicName
      );
    }

    // return result
    return result;
  }
}
