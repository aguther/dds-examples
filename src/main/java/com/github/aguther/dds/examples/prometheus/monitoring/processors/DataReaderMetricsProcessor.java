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

package com.github.aguther.dds.examples.prometheus.monitoring.processors;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.aguther.dds.util.BuiltinTopicHelper;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.rti.dds.monitoring.DataReaderDescription;
import idl.rti.dds.monitoring.DataReaderEntityStatistics;
import idl.rti.dds.monitoring.DomainParticipantDescription;
import idl.rti.dds.monitoring.SubscriberDescription;
import io.prometheus.client.Gauge;
import java.util.HashMap;

public class DataReaderMetricsProcessor {

  private final DescriptionProcessorCache descriptionProcessorCache;
  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge serializedSampleMaxSize;
  private final Gauge serializedSampleMinSize;
  private final Gauge serializedKeyMaxSize;
  private final Gauge isContentFiltered;
  private final Gauge sampleRejectedStatusTotalCount;
  private final Gauge sampleRejectedStatusLastReason;
  private final Gauge livelinessChangedStatusAliveCount;
  private final Gauge livelinessChangedStatusNotAliveCount;
  private final Gauge requestedDeadlineMissedStatusTotalCount;
  private final Gauge requestedIncompatibleQosStatusTotalCount;
  private final Gauge requestedIncompatibleQosStatusLastPolicyId;
  private final Gauge sampleLostStatusTotalCount;
  private final Gauge sampleLostStatusLastReason;
  private final Gauge subscriptionMatchedStatusTotalCount;
  private final Gauge subscriptionMatchedStatusCurrentCount;
  private final Gauge subscriptionMatchedStatusCurrentCountPeak;
  private final Gauge datareaderCacheStatusSampleCount;
  private final Gauge datareaderCacheStatusSampleCountPeak;
  private final Gauge datareaderProtocolStatusReceivedSampleCount;
  private final Gauge datareaderProtocolStatusReceivedSampleBytes;
  private final Gauge datareaderProtocolStatusDuplicateSampleCount;
  private final Gauge datareaderProtocolStatusDuplicateSampleBytes;
  private final Gauge datareaderProtocolStatusFilteredSampleCount;
  private final Gauge datareaderProtocolStatusFilteredSampleBytes;
  private final Gauge datareaderProtocolStatusReceivedHeartbeatCount;
  private final Gauge datareaderProtocolStatusReceivedHeartbeatBytes;
  private final Gauge datareaderProtocolStatusSentAckCount;
  private final Gauge datareaderProtocolStatusSentAckBytes;
  private final Gauge datareaderProtocolStatusSentNackCount;
  private final Gauge datareaderProtocolStatusSentNackBytes;
  private final Gauge datareaderProtocolStatusReceivedGapCount;
  private final Gauge datareaderProtocolStatusReceivedGapBytes;
  private final Gauge datareaderProtocolStatusRejectedSampleCount;
  private final Gauge datareaderProtocolStatusFirstAvailableSampleSequenceNumberHigh;
  private final Gauge datareaderProtocolStatusFirstAvailableSampleSequenceNumberLow;
  private final Gauge datareaderProtocolStatusLastAvailableSampleSequenceNumberHigh;
  private final Gauge datareaderProtocolStatusLastAvailableSampleSequenceNumberLow;
  private final Gauge datareaderProtocolStatusLastCommittedSampleSequenceNumberHigh;
  private final Gauge datareaderProtocolStatusLastCommittedSampleSequenceNumberLow;
  private final Gauge datareaderProtocolStatusUncommittedSampleCount;

  public DataReaderMetricsProcessor(
    DescriptionProcessorCache descriptionProcessorCache
  ) {
    checkNotNull(descriptionProcessorCache);
    this.descriptionProcessorCache = descriptionProcessorCache;
    instanceHandleHashMap = new HashMap<>();

    serializedSampleMaxSize = Gauge.build()
      .name("dds_datareader_serialized_sample_max_size_bytes")
      .labelNames(getLabelNames())
      .help("dds_datareader_serialized_sample_max_size_bytes")
      .register();

    serializedSampleMinSize = Gauge.build()
      .name("dds_datareader_serialized_sample_min_size_bytes")
      .labelNames(getLabelNames())
      .help("dds_datareader_serialized_sample_min_size_bytes")
      .register();

    serializedKeyMaxSize = Gauge.build()
      .name("dds_datareader_serialized_key_max_size_bytes")
      .labelNames(getLabelNames())
      .help("dds_datareader_serialized_key_max_size_bytes")
      .register();

    isContentFiltered = Gauge.build()
      .name("dds_datareader_is_content_filtered")
      .labelNames(getLabelNames())
      .help("dds_datareader_is_content_filtered")
      .register();

    sampleRejectedStatusTotalCount = Gauge.build()
      .name("dds_datareader_sample_rejected_status_total_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_sample_rejected_status_total_count")
      .register();

    sampleRejectedStatusLastReason = Gauge.build()
      .name("dds_datareader_sample_rejected_status_last_reason")
      .labelNames(getLabelNames())
      .help("dds_datareader_sample_rejected_status_last_reason")
      .register();

    livelinessChangedStatusAliveCount = Gauge.build()
      .name("dds_datareader_liveliness_changed_status_alive_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_liveliness_changed_status_alive_count")
      .register();

    livelinessChangedStatusNotAliveCount = Gauge.build()
      .name("dds_datareader_liveliness_changed_status_not_alive_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_liveliness_changed_status_not_alive_count")
      .register();

    requestedDeadlineMissedStatusTotalCount = Gauge.build()
      .name("dds_datareader_requested_deadline_missed_status_total_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_requested_deadline_missed_status_total_count")
      .register();

    requestedIncompatibleQosStatusTotalCount = Gauge.build()
      .name("dds_datareader_requested_incompatible_qos_status_total_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_requested_incompatible_qos_status_total_count")
      .register();

    requestedIncompatibleQosStatusLastPolicyId = Gauge.build()
      .name("dds_datareader_requested_incompatible_qos_status_last_policy_id")
      .labelNames(getLabelNames())
      .help("dds_datareader_requested_incompatible_qos_status_last_policy_id")
      .register();

    sampleLostStatusTotalCount = Gauge.build()
      .name("dds_datareader_sample_lost_status_total_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_sample_lost_status_total_count")
      .register();

    sampleLostStatusLastReason = Gauge.build()
      .name("dds_datareader_sample_lost_status_last_reason")
      .labelNames(getLabelNames())
      .help("dds_datareader_sample_lost_status_last_reason")
      .register();

    subscriptionMatchedStatusTotalCount = Gauge.build()
      .name("dds_datareader_subscription_matched_status_total_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_subscription_matched_status_total_count")
      .register();

    subscriptionMatchedStatusCurrentCount = Gauge.build()
      .name("dds_datareader_subscription_matched_status_current_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_subscription_matched_status_current_count")
      .register();

    subscriptionMatchedStatusCurrentCountPeak = Gauge.build()
      .name("dds_datareader_subscription_matched_status_current_count_peak")
      .labelNames(getLabelNames())
      .help("dds_datareader_subscription_matched_status_current_count_peak")
      .register();

    datareaderCacheStatusSampleCount = Gauge.build()
      .name("dds_datareader_cache_status_sample_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_cache_status_sample_count")
      .register();

    datareaderCacheStatusSampleCountPeak = Gauge.build()
      .name("dds_datareader_cache_status_sample_count_peak")
      .labelNames(getLabelNames())
      .help("dds_datareader_cache_status_sample_count_peak")
      .register();

    datareaderProtocolStatusReceivedSampleCount = Gauge.build()
      .name("dds_datareader_protocol_status_received_sample_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_received_sample_count")
      .register();

    datareaderProtocolStatusReceivedSampleBytes = Gauge.build()
      .name("dds_datareader_protocol_status_received_sample_bytes")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_received_sample_bytes")
      .register();

    datareaderProtocolStatusDuplicateSampleCount = Gauge.build()
      .name("dds_datareader_protocol_status_duplicate_sample_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_duplicate_sample_count")
      .register();

    datareaderProtocolStatusDuplicateSampleBytes = Gauge.build()
      .name("dds_datareader_protocol_status_duplicate_sample_bytes")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_duplicate_sample_bytes")
      .register();

    datareaderProtocolStatusFilteredSampleCount = Gauge.build()
      .name("dds_datareader_protocol_status_filtered_sample_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_filtered_sample_count")
      .register();

    datareaderProtocolStatusFilteredSampleBytes = Gauge.build()
      .name("dds_datareader_protocol_status_filtered_sample_bytes")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_filtered_sample_bytes")
      .register();

    datareaderProtocolStatusReceivedHeartbeatCount = Gauge.build()
      .name("dds_datareader_protocol_status_received_heartbeat_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_received_heartbeat_count")
      .register();

    datareaderProtocolStatusReceivedHeartbeatBytes = Gauge.build()
      .name("dds_datareader_protocol_status_received_heartbeat_bytes")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_received_heartbeat_bytes")
      .register();

    datareaderProtocolStatusSentAckCount = Gauge.build()
      .name("dds_datareader_protocol_status_sent_ack_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_sent_ack_count")
      .register();

    datareaderProtocolStatusSentAckBytes = Gauge.build()
      .name("dds_datareader_protocol_status_sent_ack_bytes")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_sent_ack_bytes")
      .register();

    datareaderProtocolStatusSentNackCount = Gauge.build()
      .name("dds_datareader_protocol_status_sent_nack_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_sent_nack_count")
      .register();

    datareaderProtocolStatusSentNackBytes = Gauge.build()
      .name("dds_datareader_protocol_status_sent_nack_bytes")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_sent_nack_bytes")
      .register();

    datareaderProtocolStatusReceivedGapCount = Gauge.build()
      .name("dds_datareader_protocol_status_received_gap_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_received_gap_count")
      .register();

    datareaderProtocolStatusReceivedGapBytes = Gauge.build()
      .name("dds_datareader_protocol_status_received_gap_bytes")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_received_gap_bytes")
      .register();

    datareaderProtocolStatusRejectedSampleCount = Gauge.build()
      .name("dds_datareader_protocol_status_rejected_sample_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_rejected_sample_count")
      .register();

    datareaderProtocolStatusFirstAvailableSampleSequenceNumberHigh = Gauge.build()
      .name("dds_datareader_protocol_status_first_available_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_first_available_sample_sequence_number_high")
      .register();

    datareaderProtocolStatusFirstAvailableSampleSequenceNumberLow = Gauge.build()
      .name("dds_datareader_protocol_status_first_available_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_first_available_sample_sequence_number_low")
      .register();

    datareaderProtocolStatusLastAvailableSampleSequenceNumberHigh = Gauge.build()
      .name("dds_datareader_protocol_status_last_available_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_last_available_sample_sequence_number_high")
      .register();

    datareaderProtocolStatusLastAvailableSampleSequenceNumberLow = Gauge.build()
      .name("dds_datareader_protocol_status_last_available_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_last_available_sample_sequence_number_low")
      .register();

    datareaderProtocolStatusLastCommittedSampleSequenceNumberHigh = Gauge.build()
      .name("dds_datareader_protocol_status_last_committed_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_last_committed_sample_sequence_number_high")
      .register();

    datareaderProtocolStatusLastCommittedSampleSequenceNumberLow = Gauge.build()
      .name("dds_datareader_protocol_status_last_committed_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_last_committed_sample_sequence_number_low")
      .register();

    datareaderProtocolStatusUncommittedSampleCount = Gauge.build()
      .name("dds_datareader_protocol_status_uncommitted_sample_count")
      .labelNames(getLabelNames())
      .help("dds_datareader_protocol_status_uncommitted_sample_count")
      .register();
  }

  public void process(
    DataReaderEntityStatistics sample,
    SampleInfo info
  ) {
    if ((info.instance_state == InstanceStateKind.ALIVE_INSTANCE_STATE)
      && (info.valid_data)
      && (descriptionProcessorCache.getDomainParticipantDescription(sample.participant_key) != null)
      && (descriptionProcessorCache.getSubscriberDescription(sample.subscriber_key) != null)
      && (descriptionProcessorCache.getDataReaderDescription(sample.datareader_key) != null)) {
      // add / update values
      addUpdateGaugesForLabel(info.instance_handle, sample);
    } else {
      // remove values
      removeLabelsFromGauges(info.instance_handle);
    }
  }

  private void addUpdateGaugesForLabel(
    InstanceHandle_t instanceHandle,
    DataReaderEntityStatistics sample
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(
      instanceHandle,
      getLabelValues(
        descriptionProcessorCache.getDomainParticipantDescription(sample.participant_key),
        descriptionProcessorCache.getSubscriberDescription(sample.subscriber_key),
        descriptionProcessorCache.getDataReaderDescription(sample.datareader_key)
      )
    );

    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(instanceHandle);

    // update gauges
    DataReaderDescription dataReaderDescription = descriptionProcessorCache
      .getDataReaderDescription(sample.datareader_key);

    serializedSampleMaxSize.labels(labelValues).set(
      dataReaderDescription.serialized_sample_max_size);

    serializedSampleMinSize.labels(labelValues).set(
      dataReaderDescription.serialized_sample_min_size);

    serializedKeyMaxSize.labels(labelValues).set(
      dataReaderDescription.serialized_key_max_size);

    isContentFiltered.labels(labelValues).set(
      sample.is_content_filtered ? 1 : 0);

    sampleRejectedStatusTotalCount.labels(labelValues).set(
      sample.sample_rejected_status.status.total_count);

    sampleRejectedStatusLastReason.labels(labelValues).set(
      sample.sample_rejected_status.status.last_reason.value());

    livelinessChangedStatusAliveCount.labels(labelValues).set(
      sample.liveliness_changed_status.status.alive_count);

    livelinessChangedStatusNotAliveCount.labels(labelValues).set(
      sample.liveliness_changed_status.status.not_alive_count);

    requestedDeadlineMissedStatusTotalCount.labels(labelValues).set(
      sample.requested_deadline_missed_status.status.total_count);

    requestedIncompatibleQosStatusTotalCount.labels(labelValues).set(
      sample.requested_incompatible_qos_status.status.total_count);

    requestedIncompatibleQosStatusLastPolicyId.labels(labelValues).set(
      sample.requested_incompatible_qos_status.status.last_policy_id);

    sampleLostStatusTotalCount.labels(labelValues).set(
      sample.sample_lost_status.status.total_count);

    sampleLostStatusLastReason.labels(labelValues).set(
      sample.sample_lost_status.status.last_reason.value());

    subscriptionMatchedStatusTotalCount.labels(labelValues).set(
      sample.subscription_matched_status.status.total_count);

    subscriptionMatchedStatusCurrentCount.labels(labelValues).set(
      sample.subscription_matched_status.status.current_count);

    subscriptionMatchedStatusCurrentCountPeak.labels(labelValues).set(
      sample.subscription_matched_status.status.current_count_peak);

    datareaderCacheStatusSampleCount.labels(labelValues).set(
      sample.datareader_cache_status.status.sample_count);

    datareaderCacheStatusSampleCountPeak.labels(labelValues).set(
      sample.datareader_cache_status.status.sample_count_peak);

    datareaderProtocolStatusReceivedSampleCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.received_sample_count);

    datareaderProtocolStatusReceivedSampleBytes.labels(labelValues).set(
      sample.datareader_protocol_status.status.received_sample_bytes);

    datareaderProtocolStatusDuplicateSampleCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.duplicate_sample_count);

    datareaderProtocolStatusDuplicateSampleBytes.labels(labelValues).set(
      sample.datareader_protocol_status.status.duplicate_sample_bytes);

    datareaderProtocolStatusFilteredSampleCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.filtered_sample_count);

    datareaderProtocolStatusFilteredSampleBytes.labels(labelValues).set(
      sample.datareader_protocol_status.status.filtered_sample_bytes);

    datareaderProtocolStatusReceivedHeartbeatCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.received_heartbeat_count);

    datareaderProtocolStatusReceivedHeartbeatBytes.labels(labelValues).set(
      sample.datareader_protocol_status.status.received_heartbeat_bytes);

    datareaderProtocolStatusSentAckCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.sent_ack_count);

    datareaderProtocolStatusSentAckBytes.labels(labelValues).set(
      sample.datareader_protocol_status.status.sent_ack_bytes);

    datareaderProtocolStatusSentNackCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.sent_nack_count);

    datareaderProtocolStatusSentNackBytes.labels(labelValues).set(
      sample.datareader_protocol_status.status.sent_nack_bytes);

    datareaderProtocolStatusReceivedGapCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.received_gap_count);

    datareaderProtocolStatusReceivedGapBytes.labels(labelValues).set(
      sample.datareader_protocol_status.status.received_gap_bytes);

    datareaderProtocolStatusRejectedSampleCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.rejected_sample_count);

    datareaderProtocolStatusFirstAvailableSampleSequenceNumberHigh.labels(labelValues).set(
      sample.datareader_protocol_status.status.first_available_sample_sequence_number.high);

    datareaderProtocolStatusFirstAvailableSampleSequenceNumberLow.labels(labelValues).set(
      sample.datareader_protocol_status.status.first_available_sample_sequence_number.low);

    datareaderProtocolStatusLastAvailableSampleSequenceNumberHigh.labels(labelValues).set(
      sample.datareader_protocol_status.status.last_available_sample_sequence_number.high);

    datareaderProtocolStatusLastAvailableSampleSequenceNumberLow.labels(labelValues).set(
      sample.datareader_protocol_status.status.last_available_sample_sequence_number.low);

    datareaderProtocolStatusLastCommittedSampleSequenceNumberHigh.labels(labelValues).set(
      sample.datareader_protocol_status.status.last_committed_sample_sequence_number.high);

    datareaderProtocolStatusLastCommittedSampleSequenceNumberLow.labels(labelValues).set(
      sample.datareader_protocol_status.status.last_committed_sample_sequence_number.low);

    datareaderProtocolStatusUncommittedSampleCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.uncommitted_sample_count);
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
    serializedSampleMaxSize.remove(labelValues);
    serializedSampleMinSize.remove(labelValues);
    serializedKeyMaxSize.remove(labelValues);
    isContentFiltered.remove(labelValues);
    sampleRejectedStatusTotalCount.remove(labelValues);
    sampleRejectedStatusLastReason.remove(labelValues);
    livelinessChangedStatusAliveCount.remove(labelValues);
    livelinessChangedStatusNotAliveCount.remove(labelValues);
    requestedDeadlineMissedStatusTotalCount.remove(labelValues);
    requestedIncompatibleQosStatusTotalCount.remove(labelValues);
    requestedIncompatibleQosStatusLastPolicyId.remove(labelValues);
    sampleLostStatusTotalCount.remove(labelValues);
    sampleLostStatusLastReason.remove(labelValues);
    subscriptionMatchedStatusTotalCount.remove(labelValues);
    subscriptionMatchedStatusCurrentCount.remove(labelValues);
    subscriptionMatchedStatusCurrentCountPeak.remove(labelValues);
    datareaderCacheStatusSampleCount.remove(labelValues);
    datareaderCacheStatusSampleCountPeak.remove(labelValues);
    datareaderProtocolStatusReceivedSampleCount.remove(labelValues);
    datareaderProtocolStatusReceivedSampleBytes.remove(labelValues);
    datareaderProtocolStatusDuplicateSampleCount.remove(labelValues);
    datareaderProtocolStatusDuplicateSampleBytes.remove(labelValues);
    datareaderProtocolStatusFilteredSampleCount.remove(labelValues);
    datareaderProtocolStatusFilteredSampleBytes.remove(labelValues);
    datareaderProtocolStatusReceivedHeartbeatCount.remove(labelValues);
    datareaderProtocolStatusReceivedHeartbeatBytes.remove(labelValues);
    datareaderProtocolStatusSentAckCount.remove(labelValues);
    datareaderProtocolStatusSentAckBytes.remove(labelValues);
    datareaderProtocolStatusSentNackCount.remove(labelValues);
    datareaderProtocolStatusSentNackBytes.remove(labelValues);
    datareaderProtocolStatusReceivedGapCount.remove(labelValues);
    datareaderProtocolStatusReceivedGapBytes.remove(labelValues);
    datareaderProtocolStatusRejectedSampleCount.remove(labelValues);
    datareaderProtocolStatusFirstAvailableSampleSequenceNumberHigh.remove(labelValues);
    datareaderProtocolStatusFirstAvailableSampleSequenceNumberLow.remove(labelValues);
    datareaderProtocolStatusLastAvailableSampleSequenceNumberHigh.remove(labelValues);
    datareaderProtocolStatusLastAvailableSampleSequenceNumberLow.remove(labelValues);
    datareaderProtocolStatusLastCommittedSampleSequenceNumberHigh.remove(labelValues);
    datareaderProtocolStatusLastCommittedSampleSequenceNumberLow.remove(labelValues);
    datareaderProtocolStatusUncommittedSampleCount.remove(labelValues);

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
      "subscriber_key",
      "subscriber_name",
      "subscriber_role_name",
      "datareader_key",
      "subscription_name",
      "subscription_role_name",
    };
  }

  private String[] getLabelValues(
    DomainParticipantDescription domainParticipantDescription,
    SubscriberDescription subscriberDescription,
    DataReaderDescription dataReaderDescription
  ) {
    return new String[]{
      BuiltinTopicHelper.toString(domainParticipantDescription.entity_key.value),
      Integer.toUnsignedString(domainParticipantDescription.domain_id),
      Integer.toUnsignedString(domainParticipantDescription.host_id),
      Integer.toUnsignedString(domainParticipantDescription.process_id),
      domainParticipantDescription.qos.participant_name.name,
      domainParticipantDescription.qos.participant_name.role_name,
      BuiltinTopicHelper.toString(dataReaderDescription.topic_entity_key.value),
      dataReaderDescription.topic_name,
      dataReaderDescription.type_name,
      BuiltinTopicHelper.toString(subscriberDescription.entity_key.value),
      subscriberDescription.qos.subscriber_name.name,
      subscriberDescription.qos.subscriber_name.role_name,
      BuiltinTopicHelper.toString(dataReaderDescription.entity_key.value),
      dataReaderDescription.qos.subscription_name.name,
      dataReaderDescription.qos.subscription_name.role_name,
    };
  }
}
