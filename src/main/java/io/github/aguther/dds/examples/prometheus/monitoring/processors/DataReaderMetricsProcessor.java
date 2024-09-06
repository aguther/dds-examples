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

package io.github.aguther.dds.examples.prometheus.monitoring.processors;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.aguther.dds.util.BuiltinTopicHelper;
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
      .help("Cumulative count of all the DDS samples that have been rejected by the DataReader.")
      .register();

    sampleRejectedStatusLastReason = Gauge.build()
      .name("dds_data_reader_sample_rejected_status_last_reason")
      .labelNames(getLabelNames())
      .help("Reason for rejecting the last DDS sample. See Table 7.13 DDS_SampleRejectedStatusKind.")
      .register();

    livelinessChangedStatusAliveCount = Gauge.build()
      .name("dds_data_reader_liveliness_changed_status_alive_count")
      .labelNames(getLabelNames())
      .help("Number of matched DataWriters that are currently alive.")
      .register();

    livelinessChangedStatusNotAliveCount = Gauge.build()
      .name("dds_data_reader_liveliness_changed_status_not_alive_count")
      .labelNames(getLabelNames())
      .help("Number of matched DataWriters that are not currently alive.")
      .register();

    requestedDeadlineMissedStatusTotalCount = Gauge.build()
      .name("dds_data_reader_requested_deadline_missed_status_total_count")
      .labelNames(getLabelNames())
      .help("Cumulative number of times that the deadline was violated for any instance read by the DataReader.")
      .register();

    requestedIncompatibleQosStatusTotalCount = Gauge.build()
      .name("dds_data_reader_requested_incompatible_qos_status_total_count")
      .labelNames(getLabelNames())
      .help(
        "Cumulative number of times the DataReader discovered a DataWriter for the same Topic with an offered QoS that is incompatible with that requested by the DataReader.")
      .register();

    requestedIncompatibleQosStatusLastPolicyId = Gauge.build()
      .name("dds_data_reader_requested_incompatible_qos_status_last_policy_id")
      .labelNames(getLabelNames())
      .help(
        "The ID of the QosPolicy that was found to be incompatible the last time an incompatibility was detected. (Note: if there are multiple incompatible policies, only one of them is reported here.)")
      .register();

    sampleLostStatusTotalCount = Gauge.build()
      .name("dds_data_reader_sample_lost_status_total_count")
      .labelNames(getLabelNames())
      .help("Cumulative count of all the DDS samples that have been lost, across all instances of data written for the Topic.")
      .register();

    sampleLostStatusLastReason = Gauge.build()
      .name("dds_data_reader_sample_lost_status_last_reason")
      .labelNames(getLabelNames())
      .help("The reason the last DDS sample was lost. See Table 7.11 DDS_SampleLostStatusKind.")
      .register();

    subscriptionMatchedStatusTotalCount = Gauge.build()
      .name("dds_data_reader_subscription_matched_status_total_count")
      .labelNames(getLabelNames())
      .help("Cumulative number of times the DataReader discovered a \"match\" with a DataWriter.")
      .register();

    subscriptionMatchedStatusCurrentCount = Gauge.build()
      .name("dds_data_reader_subscription_matched_status_current_count")
      .labelNames(getLabelNames())
      .help("The number of DataWriters currently matched to the concerned DataReader.")
      .register();

    subscriptionMatchedStatusCurrentCountPeak = Gauge.build()
      .name("dds_data_reader_subscription_matched_status_current_count_peak")
      .labelNames(getLabelNames())
      .help("The highest value that current_count has reached until now.")
      .register();

    dataReaderCacheStatusSampleCount = Gauge.build()
      .name("dds_data_reader_cache_status_sample_count")
      .labelNames(getLabelNames())
      .help(
        "Current number of DDS samples in the DataReader’s queue. Includes DDS samples that may not yet be available to be read or taken by the user due to DDS samples being received out of order or settings in the 6.4.6 PRESENTATION QosPolicy.")
      .register();

    dataReaderCacheStatusSampleCountPeak = Gauge.build()
      .name("dds_data_reader_cache_status_sample_count_peak")
      .labelNames(getLabelNames())
      .help("Highest number of DDS samples in the DataReader’s queue over the lifetime of the DataReader.")
      .register();

    dataReaderProtocolStatusReceivedSampleCount = Gauge.build()
      .name("dds_data_reader_protocol_status_received_sample_count")
      .labelNames(getLabelNames())
      .help("The number of samples received by a DataReader.")
      .register();

    dataReaderProtocolStatusReceivedSampleBytes = Gauge.build()
      .name("dds_data_reader_protocol_status_received_sample_bytes")
      .labelNames(getLabelNames())
      .help("The number of bytes received by a DataReader.")
      .register();

    dataReaderProtocolStatusDuplicateSampleCount = Gauge.build()
      .name("dds_data_reader_protocol_status_duplicate_sample_count")
      .labelNames(getLabelNames())
      .help(
        "The number of DDS samples from a remote DataWriter received, not for the first time, by a local DataReader.")
      .register();

    dataReaderProtocolStatusDuplicateSampleBytes = Gauge.build()
      .name("dds_data_reader_protocol_status_duplicate_sample_bytes")
      .labelNames(getLabelNames())
      .help(
        "The number of bytes of DDS samples from a remote DataWriter received, not for the first time, by a local DataReader.")
      .register();

    dataReaderProtocolStatusFilteredSampleCount = Gauge.build()
      .name("dds_data_reader_protocol_status_filtered_sample_count")
      .labelNames(getLabelNames())
      .help(
        "The number of DDS samples filtered by the local DataReader due to ContentFilteredTopics or Time-Based Filter.")
      .register();

    dataReaderProtocolStatusFilteredSampleBytes = Gauge.build()
      .name("dds_data_reader_protocol_status_filtered_sample_bytes")
      .labelNames(getLabelNames())
      .help(
        "The number of bytes of DDS samples filtered by the local DataReader due to ContentFilteredTopics or Time-Based Filter.")
      .register();

    dataReaderProtocolStatusReceivedHeartbeatCount = Gauge.build()
      .name("dds_data_reader_protocol_status_received_heartbeat_count")
      .labelNames(getLabelNames())
      .help("The number of Heartbeats from a remote DataWriter received by a local DataReader.")
      .register();

    dataReaderProtocolStatusReceivedHeartbeatBytes = Gauge.build()
      .name("dds_data_reader_protocol_status_received_heartbeat_bytes")
      .labelNames(getLabelNames())
      .help("The number of bytes of Heartbeats from a remote DataWriter received by a local DataReader.")
      .register();

    dataReaderProtocolStatusSentAckCount = Gauge.build()
      .name("dds_data_reader_protocol_status_sent_ack_count")
      .labelNames(getLabelNames())
      .help("The number of ACKs sent from a local DataReader to a matching remote DataWriter.")
      .register();

    dataReaderProtocolStatusSentAckBytes = Gauge.build()
      .name("dds_data_reader_protocol_status_sent_ack_bytes")
      .labelNames(getLabelNames())
      .help("The number of bytes of ACKs sent from a local DataReader to a matching remote DataWriter.")
      .register();

    dataReaderProtocolStatusSentNackCount = Gauge.build()
      .name("dds_data_reader_protocol_status_sent_nack_count")
      .labelNames(getLabelNames())
      .help("The number of NACKs sent from a local DataReader to a matching remote DataWriter.")
      .register();

    dataReaderProtocolStatusSentNackBytes = Gauge.build()
      .name("dds_data_reader_protocol_status_sent_nack_bytes")
      .labelNames(getLabelNames())
      .help("The number of bytes of NACKs sent from a local DataReader to a matching remote DataWriter.")
      .register();

    dataReaderProtocolStatusReceivedGapCount = Gauge.build()
      .name("dds_data_reader_protocol_status_received_gap_count")
      .labelNames(getLabelNames())
      .help("The number of GAPs received from remote DataWriter to this DataReader.")
      .register();

    dataReaderProtocolStatusReceivedGapBytes = Gauge.build()
      .name("dds_data_reader_protocol_status_received_gap_bytes")
      .labelNames(getLabelNames())
      .help("The number of bytes of GAPs received from remote DataWriter to this DataReader.")
      .register();

    dataReaderProtocolStatusRejectedSampleCount = Gauge.build()
      .name("dds_data_reader_protocol_status_rejected_sample_count")
      .labelNames(getLabelNames())
      .help("The number of times a DDS sample is rejected for unanticipated reasons in the receive path.")
      .register();

    dataReaderProtocolStatusFirstAvailableSampleSequenceNumberHigh = Gauge.build()
      .name("dds_data_reader_protocol_status_first_available_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "Sequence number of the first available DDS sample in a matched DataWriter's reliability queue. Applicable only when retrieving matched DataWriter statuses.")
      .register();

    dataReaderProtocolStatusFirstAvailableSampleSequenceNumberLow = Gauge.build()
      .name("dds_data_reader_protocol_status_first_available_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "Sequence number of the first available DDS sample in a matched DataWriter's reliability queue. Applicable only when retrieving matched DataWriter statuses.")
      .register();

    dataReaderProtocolStatusLastAvailableSampleSequenceNumberHigh = Gauge.build()
      .name("dds_data_reader_protocol_status_last_available_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "Sequence number of the last available DDS sample in a matched DataWriter's reliability queue. Applicable only when retrieving matched DataWriter statuses.")
      .register();

    dataReaderProtocolStatusLastAvailableSampleSequenceNumberLow = Gauge.build()
      .name("dds_data_reader_protocol_status_last_available_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "Sequence number of the last available DDS sample in a matched DataWriter's reliability queue. Applicable only when retrieving matched DataWriter statuses.")
      .register();

    dataReaderProtocolStatusLastCommittedSampleSequenceNumberHigh = Gauge.build()
      .name("dds_data_reader_protocol_status_last_committed_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "Sequence number of the last committed DDS sample (i.e. available to be read or taken) in a matched DataWriter's reliability queue. Applicable only when retrieving matched DataWriter statuses. For best-effort DataReaders, this is the sequence number of the latest DDS sample received. For reliable DataReaders, this is the sequence number of the latest DDS sample that is available to be read or taken from the DataReader's queue.")
      .register();

    dataReaderProtocolStatusLastCommittedSampleSequenceNumberLow = Gauge.build()
      .name("dds_data_reader_protocol_status_last_committed_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "Number of received DDS samples that are not yet available to be read or taken due to being received out of order. Applicable only when retrieving matched DataWriter statuses.")
      .register();

    dataReaderProtocolStatusUncommittedSampleCount = Gauge.build()
      .name("dds_data_reader_protocol_status_uncommitted_sample_count")
      .labelNames(getLabelNames())
      .help(
        "Number of received DDS samples that are not yet available to be read or taken due to being received out of order. Applicable only when retrieving matched DataWriter statuses.")
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
