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
  private final Gauge dataReaderCacheStatusSampleCount;
  private final Gauge dataReaderCacheStatusSampleCountPeak;
  private final Gauge dataReaderProtocolStatusReceivedSampleCount;
  private final Gauge dataReaderProtocolStatusReceivedSampleBytes;
  private final Gauge dataReaderProtocolStatusDuplicateSampleCount;
  private final Gauge dataReaderProtocolStatusDuplicateSampleBytes;
  private final Gauge dataReaderProtocolStatusFilteredSampleCount;
  private final Gauge dataReaderProtocolStatusFilteredSampleBytes;
  private final Gauge dataReaderProtocolStatusReceivedHeartbeatCount;
  private final Gauge dataReaderProtocolStatusReceivedHeartbeatBytes;
  private final Gauge dataReaderProtocolStatusSentAckCount;
  private final Gauge dataReaderProtocolStatusSentAckBytes;
  private final Gauge dataReaderProtocolStatusSentNackCount;
  private final Gauge dataReaderProtocolStatusSentNackBytes;
  private final Gauge dataReaderProtocolStatusReceivedGapCount;
  private final Gauge dataReaderProtocolStatusReceivedGapBytes;
  private final Gauge dataReaderProtocolStatusRejectedSampleCount;
  private final Gauge dataReaderProtocolStatusFirstAvailableSampleSequenceNumberHigh;
  private final Gauge dataReaderProtocolStatusFirstAvailableSampleSequenceNumberLow;
  private final Gauge dataReaderProtocolStatusLastAvailableSampleSequenceNumberHigh;
  private final Gauge dataReaderProtocolStatusLastAvailableSampleSequenceNumberLow;
  private final Gauge dataReaderProtocolStatusLastCommittedSampleSequenceNumberHigh;
  private final Gauge dataReaderProtocolStatusLastCommittedSampleSequenceNumberLow;
  private final Gauge dataReaderProtocolStatusUncommittedSampleCount;

  public DataReaderMetricsProcessor(
    DescriptionProcessorCache descriptionProcessorCache
  ) {
    checkNotNull(descriptionProcessorCache);
    this.descriptionProcessorCache = descriptionProcessorCache;
    instanceHandleHashMap = new HashMap<>();

    serializedSampleMaxSize = Gauge.build()
      .name("dds_data_reader_serialized_sample_max_size_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_serialized_sample_max_size_bytes")
      .register();

    serializedSampleMinSize = Gauge.build()
      .name("dds_data_reader_serialized_sample_min_size_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_serialized_sample_min_size_bytes")
      .register();

    serializedKeyMaxSize = Gauge.build()
      .name("dds_data_reader_serialized_key_max_size_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_serialized_key_max_size_bytes")
      .register();

    isContentFiltered = Gauge.build()
      .name("dds_data_reader_is_content_filtered")
      .labelNames(getLabelNames())
      .help("dds_data_reader_is_content_filtered")
      .register();

    sampleRejectedStatusTotalCount = Gauge.build()
      .name("dds_data_reader_sample_rejected_status_total_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_sample_rejected_status_total_count")
      .register();

    sampleRejectedStatusLastReason = Gauge.build()
      .name("dds_data_reader_sample_rejected_status_last_reason")
      .labelNames(getLabelNames())
      .help("dds_data_reader_sample_rejected_status_last_reason")
      .register();

    livelinessChangedStatusAliveCount = Gauge.build()
      .name("dds_data_reader_liveliness_changed_status_alive_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_liveliness_changed_status_alive_count")
      .register();

    livelinessChangedStatusNotAliveCount = Gauge.build()
      .name("dds_data_reader_liveliness_changed_status_not_alive_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_liveliness_changed_status_not_alive_count")
      .register();

    requestedDeadlineMissedStatusTotalCount = Gauge.build()
      .name("dds_data_reader_requested_deadline_missed_status_total_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_requested_deadline_missed_status_total_count")
      .register();

    requestedIncompatibleQosStatusTotalCount = Gauge.build()
      .name("dds_data_reader_requested_incompatible_qos_status_total_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_requested_incompatible_qos_status_total_count")
      .register();

    requestedIncompatibleQosStatusLastPolicyId = Gauge.build()
      .name("dds_data_reader_requested_incompatible_qos_status_last_policy_id")
      .labelNames(getLabelNames())
      .help("dds_data_reader_requested_incompatible_qos_status_last_policy_id")
      .register();

    sampleLostStatusTotalCount = Gauge.build()
      .name("dds_data_reader_sample_lost_status_total_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_sample_lost_status_total_count")
      .register();

    sampleLostStatusLastReason = Gauge.build()
      .name("dds_data_reader_sample_lost_status_last_reason")
      .labelNames(getLabelNames())
      .help("dds_data_reader_sample_lost_status_last_reason")
      .register();

    subscriptionMatchedStatusTotalCount = Gauge.build()
      .name("dds_data_reader_subscription_matched_status_total_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_subscription_matched_status_total_count")
      .register();

    subscriptionMatchedStatusCurrentCount = Gauge.build()
      .name("dds_data_reader_subscription_matched_status_current_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_subscription_matched_status_current_count")
      .register();

    subscriptionMatchedStatusCurrentCountPeak = Gauge.build()
      .name("dds_data_reader_subscription_matched_status_current_count_peak")
      .labelNames(getLabelNames())
      .help("dds_data_reader_subscription_matched_status_current_count_peak")
      .register();

    dataReaderCacheStatusSampleCount = Gauge.build()
      .name("dds_data_reader_cache_status_sample_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_cache_status_sample_count")
      .register();

    dataReaderCacheStatusSampleCountPeak = Gauge.build()
      .name("dds_data_reader_cache_status_sample_count_peak")
      .labelNames(getLabelNames())
      .help("dds_data_reader_cache_status_sample_count_peak")
      .register();

    dataReaderProtocolStatusReceivedSampleCount = Gauge.build()
      .name("dds_data_reader_protocol_status_received_sample_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_received_sample_count")
      .register();

    dataReaderProtocolStatusReceivedSampleBytes = Gauge.build()
      .name("dds_data_reader_protocol_status_received_sample_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_received_sample_bytes")
      .register();

    dataReaderProtocolStatusDuplicateSampleCount = Gauge.build()
      .name("dds_data_reader_protocol_status_duplicate_sample_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_duplicate_sample_count")
      .register();

    dataReaderProtocolStatusDuplicateSampleBytes = Gauge.build()
      .name("dds_data_reader_protocol_status_duplicate_sample_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_duplicate_sample_bytes")
      .register();

    dataReaderProtocolStatusFilteredSampleCount = Gauge.build()
      .name("dds_data_reader_protocol_status_filtered_sample_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_filtered_sample_count")
      .register();

    dataReaderProtocolStatusFilteredSampleBytes = Gauge.build()
      .name("dds_data_reader_protocol_status_filtered_sample_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_filtered_sample_bytes")
      .register();

    dataReaderProtocolStatusReceivedHeartbeatCount = Gauge.build()
      .name("dds_data_reader_protocol_status_received_heartbeat_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_received_heartbeat_count")
      .register();

    dataReaderProtocolStatusReceivedHeartbeatBytes = Gauge.build()
      .name("dds_data_reader_protocol_status_received_heartbeat_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_received_heartbeat_bytes")
      .register();

    dataReaderProtocolStatusSentAckCount = Gauge.build()
      .name("dds_data_reader_protocol_status_sent_ack_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_sent_ack_count")
      .register();

    dataReaderProtocolStatusSentAckBytes = Gauge.build()
      .name("dds_data_reader_protocol_status_sent_ack_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_sent_ack_bytes")
      .register();

    dataReaderProtocolStatusSentNackCount = Gauge.build()
      .name("dds_data_reader_protocol_status_sent_nack_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_sent_nack_count")
      .register();

    dataReaderProtocolStatusSentNackBytes = Gauge.build()
      .name("dds_data_reader_protocol_status_sent_nack_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_sent_nack_bytes")
      .register();

    dataReaderProtocolStatusReceivedGapCount = Gauge.build()
      .name("dds_data_reader_protocol_status_received_gap_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_received_gap_count")
      .register();

    dataReaderProtocolStatusReceivedGapBytes = Gauge.build()
      .name("dds_data_reader_protocol_status_received_gap_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_received_gap_bytes")
      .register();

    dataReaderProtocolStatusRejectedSampleCount = Gauge.build()
      .name("dds_data_reader_protocol_status_rejected_sample_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_rejected_sample_count")
      .register();

    dataReaderProtocolStatusFirstAvailableSampleSequenceNumberHigh = Gauge.build()
      .name("dds_data_reader_protocol_status_first_available_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_first_available_sample_sequence_number_high")
      .register();

    dataReaderProtocolStatusFirstAvailableSampleSequenceNumberLow = Gauge.build()
      .name("dds_data_reader_protocol_status_first_available_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_first_available_sample_sequence_number_low")
      .register();

    dataReaderProtocolStatusLastAvailableSampleSequenceNumberHigh = Gauge.build()
      .name("dds_data_reader_protocol_status_last_available_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_last_available_sample_sequence_number_high")
      .register();

    dataReaderProtocolStatusLastAvailableSampleSequenceNumberLow = Gauge.build()
      .name("dds_data_reader_protocol_status_last_available_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_last_available_sample_sequence_number_low")
      .register();

    dataReaderProtocolStatusLastCommittedSampleSequenceNumberHigh = Gauge.build()
      .name("dds_data_reader_protocol_status_last_committed_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_last_committed_sample_sequence_number_high")
      .register();

    dataReaderProtocolStatusLastCommittedSampleSequenceNumberLow = Gauge.build()
      .name("dds_data_reader_protocol_status_last_committed_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_last_committed_sample_sequence_number_low")
      .register();

    dataReaderProtocolStatusUncommittedSampleCount = Gauge.build()
      .name("dds_data_reader_protocol_status_uncommitted_sample_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_protocol_status_uncommitted_sample_count")
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

    dataReaderCacheStatusSampleCount.labels(labelValues).set(
      sample.datareader_cache_status.status.sample_count);

    dataReaderCacheStatusSampleCountPeak.labels(labelValues).set(
      sample.datareader_cache_status.status.sample_count_peak);

    dataReaderProtocolStatusReceivedSampleCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.received_sample_count);

    dataReaderProtocolStatusReceivedSampleBytes.labels(labelValues).set(
      sample.datareader_protocol_status.status.received_sample_bytes);

    dataReaderProtocolStatusDuplicateSampleCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.duplicate_sample_count);

    dataReaderProtocolStatusDuplicateSampleBytes.labels(labelValues).set(
      sample.datareader_protocol_status.status.duplicate_sample_bytes);

    dataReaderProtocolStatusFilteredSampleCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.filtered_sample_count);

    dataReaderProtocolStatusFilteredSampleBytes.labels(labelValues).set(
      sample.datareader_protocol_status.status.filtered_sample_bytes);

    dataReaderProtocolStatusReceivedHeartbeatCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.received_heartbeat_count);

    dataReaderProtocolStatusReceivedHeartbeatBytes.labels(labelValues).set(
      sample.datareader_protocol_status.status.received_heartbeat_bytes);

    dataReaderProtocolStatusSentAckCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.sent_ack_count);

    dataReaderProtocolStatusSentAckBytes.labels(labelValues).set(
      sample.datareader_protocol_status.status.sent_ack_bytes);

    dataReaderProtocolStatusSentNackCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.sent_nack_count);

    dataReaderProtocolStatusSentNackBytes.labels(labelValues).set(
      sample.datareader_protocol_status.status.sent_nack_bytes);

    dataReaderProtocolStatusReceivedGapCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.received_gap_count);

    dataReaderProtocolStatusReceivedGapBytes.labels(labelValues).set(
      sample.datareader_protocol_status.status.received_gap_bytes);

    dataReaderProtocolStatusRejectedSampleCount.labels(labelValues).set(
      sample.datareader_protocol_status.status.rejected_sample_count);

    dataReaderProtocolStatusFirstAvailableSampleSequenceNumberHigh.labels(labelValues).set(
      sample.datareader_protocol_status.status.first_available_sample_sequence_number.high);

    dataReaderProtocolStatusFirstAvailableSampleSequenceNumberLow.labels(labelValues).set(
      sample.datareader_protocol_status.status.first_available_sample_sequence_number.low);

    dataReaderProtocolStatusLastAvailableSampleSequenceNumberHigh.labels(labelValues).set(
      sample.datareader_protocol_status.status.last_available_sample_sequence_number.high);

    dataReaderProtocolStatusLastAvailableSampleSequenceNumberLow.labels(labelValues).set(
      sample.datareader_protocol_status.status.last_available_sample_sequence_number.low);

    dataReaderProtocolStatusLastCommittedSampleSequenceNumberHigh.labels(labelValues).set(
      sample.datareader_protocol_status.status.last_committed_sample_sequence_number.high);

    dataReaderProtocolStatusLastCommittedSampleSequenceNumberLow.labels(labelValues).set(
      sample.datareader_protocol_status.status.last_committed_sample_sequence_number.low);

    dataReaderProtocolStatusUncommittedSampleCount.labels(labelValues).set(
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
    dataReaderCacheStatusSampleCount.remove(labelValues);
    dataReaderCacheStatusSampleCountPeak.remove(labelValues);
    dataReaderProtocolStatusReceivedSampleCount.remove(labelValues);
    dataReaderProtocolStatusReceivedSampleBytes.remove(labelValues);
    dataReaderProtocolStatusDuplicateSampleCount.remove(labelValues);
    dataReaderProtocolStatusDuplicateSampleBytes.remove(labelValues);
    dataReaderProtocolStatusFilteredSampleCount.remove(labelValues);
    dataReaderProtocolStatusFilteredSampleBytes.remove(labelValues);
    dataReaderProtocolStatusReceivedHeartbeatCount.remove(labelValues);
    dataReaderProtocolStatusReceivedHeartbeatBytes.remove(labelValues);
    dataReaderProtocolStatusSentAckCount.remove(labelValues);
    dataReaderProtocolStatusSentAckBytes.remove(labelValues);
    dataReaderProtocolStatusSentNackCount.remove(labelValues);
    dataReaderProtocolStatusSentNackBytes.remove(labelValues);
    dataReaderProtocolStatusReceivedGapCount.remove(labelValues);
    dataReaderProtocolStatusReceivedGapBytes.remove(labelValues);
    dataReaderProtocolStatusRejectedSampleCount.remove(labelValues);
    dataReaderProtocolStatusFirstAvailableSampleSequenceNumberHigh.remove(labelValues);
    dataReaderProtocolStatusFirstAvailableSampleSequenceNumberLow.remove(labelValues);
    dataReaderProtocolStatusLastAvailableSampleSequenceNumberHigh.remove(labelValues);
    dataReaderProtocolStatusLastAvailableSampleSequenceNumberLow.remove(labelValues);
    dataReaderProtocolStatusLastCommittedSampleSequenceNumberHigh.remove(labelValues);
    dataReaderProtocolStatusLastCommittedSampleSequenceNumberLow.remove(labelValues);
    dataReaderProtocolStatusUncommittedSampleCount.remove(labelValues);

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
