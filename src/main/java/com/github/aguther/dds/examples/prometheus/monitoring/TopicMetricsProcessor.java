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

package com.github.aguther.dds.examples.prometheus.monitoring;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.aguther.dds.util.BuiltinTopicHelper;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.rti.dds.monitoring.DomainParticipantDescription;
import idl.rti.dds.monitoring.TopicDescription;
import idl.rti.dds.monitoring.TopicEntityStatistics;
import io.prometheus.client.Gauge;
import java.util.HashMap;

public class TopicMetricsProcessor {

  private final DescriptionProcessorCache descriptionProcessorCache;
  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge tcSerializedSize;
  private final Gauge inconsistentTopicStatusTotalCount;

  public TopicMetricsProcessor(
    DescriptionProcessorCache descriptionProcessorCache
  ) {
    checkNotNull(descriptionProcessorCache);
    this.descriptionProcessorCache = descriptionProcessorCache;
    instanceHandleHashMap = new HashMap<>();

    tcSerializedSize = Gauge.build()
      .name("topic_type_serialized_size_bytes")
      .labelNames(getLabelNames())
      .help("topic_type_serialized_size_bytes")
      .register();

    inconsistentTopicStatusTotalCount = Gauge.build()
      .name("topic_inconsistent_topic_status_total_count")
      .labelNames(getLabelNames())
      .help("topic_inconsistent_topic_status_total_count")
      .register();
  }

  public void process(
    TopicEntityStatistics sample,
    SampleInfo info
  ) {
    if ((info.instance_state == InstanceStateKind.ALIVE_INSTANCE_STATE)
      && (info.valid_data)
      && (descriptionProcessorCache.getDomainParticipantDescription(sample.participant_key) != null)
      && (descriptionProcessorCache.getTopicDescription(sample.topic_key) != null)) {
      // add / update values
      addUpdateGaugesForLabel(info.instance_handle, sample);
    } else {
      // remove values
      removeLabelsFromGauges(info.instance_handle);
    }
  }

  private void addUpdateGaugesForLabel(
    InstanceHandle_t instanceHandle,
    TopicEntityStatistics sample
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(
      instanceHandle,
      getLabelValues(
        descriptionProcessorCache.getDomainParticipantDescription(sample.participant_key),
        descriptionProcessorCache.getTopicDescription(sample.topic_key)
      )
    );

    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(instanceHandle);

    // update gauges
    TopicDescription topicDescription = descriptionProcessorCache.getTopicDescription(sample.topic_key);
    tcSerializedSize.labels(labelValues).set(topicDescription.tc_serialized_size);
    inconsistentTopicStatusTotalCount.labels(labelValues).set(sample.inconsistent_topic_status.status.total_count);
  }

  private void removeLabelsFromGauges(
    InstanceHandle_t instanceHandle
  ) {
    // check if remove is necessary
    if (!instanceHandleHashMap.containsKey(instanceHandle)) {
      return;
    }

    // get label values
    final String[] labelValues = instanceHandleHashMap.get(instanceHandle);

    // remove labels
    tcSerializedSize.remove(labelValues);
    inconsistentTopicStatusTotalCount.remove(labelValues);

    // remove instance from hash map
    instanceHandleHashMap.remove(instanceHandle);
  }

  private String[] getLabelNames() {
    return new String[]{
      "participant_key",
      "domain_id",
      "host_id",
      "process_id",
      "participant_name",
      "participant_role_name",
      "topic_key",
      "topic_name",
      "type_name",
    };
  }

  private String[] getLabelValues(
    DomainParticipantDescription domainParticipantDescription,
    TopicDescription topicDescription
  ) {
    return new String[]{
      BuiltinTopicHelper.toString(domainParticipantDescription.entity_key.value),
      Integer.toUnsignedString(domainParticipantDescription.domain_id),
      Integer.toUnsignedString(domainParticipantDescription.host_id),
      Integer.toUnsignedString(domainParticipantDescription.process_id),
      domainParticipantDescription.qos.participant_name.name,
      domainParticipantDescription.qos.participant_name.role_name,
      BuiltinTopicHelper.toString(topicDescription.entity_key.value),
      topicDescription.topic_name,
      topicDescription.type_name,
    };
  }
}
