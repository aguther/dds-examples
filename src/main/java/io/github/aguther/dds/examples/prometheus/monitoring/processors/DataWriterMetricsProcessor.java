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
import idl.rti.dds.monitoring.DataWriterDescription;
import idl.rti.dds.monitoring.DataWriterEntityStatistics;
import idl.rti.dds.monitoring.DomainParticipantDescription;
import idl.rti.dds.monitoring.PublisherDescription;
import io.prometheus.client.Gauge;
import java.util.HashMap;

public class DataWriterMetricsProcessor {

  private final DescriptionProcessorCache descriptionProcessorCache;
  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge serializedSampleMaxSize;
  private final Gauge serializedSampleMinSize;
  private final Gauge serializedKeyMaxSize;
  private final Gauge livelinessLostStatusTotalCount;
  private final Gauge offeredDeadlineMissedStatusTotalCount;
  private final Gauge offeredIncompatibleQosStatusTotalCount;
  private final Gauge offeredIncompatibleQosStatusLastPolicyId;
  private final Gauge publicationMatchedStatusTotalCount;
  private final Gauge publicationMatchedStatusCurrentCount;
  private final Gauge publicationMatchedStatusCurrentCountPeak;
  private final Gauge reliableWriterCacheChangedStatusEmptyTotalCount;
  private final Gauge reliableWriterCacheChangedStatusFullTotalCount;
  private final Gauge reliableWriterCacheChangedStatusLowWatermarkTotalCount;
  private final Gauge reliableWriterCacheChangedStatusHighWatermarkTotalCount;
  private final Gauge reliableWriterCacheChangedStatusUnacknowledgedSampleCount;
  private final Gauge reliableWriterCacheChangedStatusUnacknowledgedSampleCountPeak;
  private final Gauge reliableReaderActivityChangedStatusActiveCount;
  private final Gauge reliableReaderActivityChangedStatusInactiveCount;
  private final Gauge dataWriterCacheStatusSampleCount;
  private final Gauge dataWriterCacheStatusSampleCountPeak;
  private final Gauge dataWriterProtocolStatusPushedSampleCount;
  private final Gauge dataWriterProtocolStatusPushedSampleBytes;
  private final Gauge dataWriterProtocolStatusFilteredSampleCount;
  private final Gauge dataWriterProtocolStatusFilteredSampleBytes;
  private final Gauge dataWriterProtocolStatusSentHeartbeatCount;
  private final Gauge dataWriterProtocolStatusSentHeartbeatBytes;
  private final Gauge dataWriterProtocolStatusPulledSampleCount;
  private final Gauge dataWriterProtocolStatusPulledSampleBytes;
  private final Gauge dataWriterProtocolStatusReceivedAckCount;
  private final Gauge dataWriterProtocolStatusReceivedAckBytes;
  private final Gauge dataWriterProtocolStatusReceivedNackCount;
  private final Gauge dataWriterProtocolStatusReceivedNackBytes;
  private final Gauge dataWriterProtocolStatusSentGapCount;
  private final Gauge dataWriterProtocolStatusSentGapBytes;
  private final Gauge dataWriterProtocolStatusRejectedSampleCount;
  private final Gauge dataWriterProtocolStatusSendWindowSize;
  private final Gauge dataWriterProtocolStatusFirstAvailableSequenceNumberHigh;
  private final Gauge dataWriterProtocolStatusFirstAvailableSequenceNumberLow;
  private final Gauge dataWriterProtocolStatusLastAvailableSequenceNumberHigh;
  private final Gauge dataWriterProtocolStatusLastAvailableSequenceNumberLow;
  private final Gauge dataWriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberHigh;
  private final Gauge dataWriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberLow;
  private final Gauge dataWriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberHigh;
  private final Gauge dataWriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberLow;
  private final Gauge dataWriterProtocolStatusLastAvailableSampleVirtualSequenceNumberHigh;
  private final Gauge dataWriterProtocolStatusLastAvailableSampleVirtualSequenceNumberLow;
  private final Gauge dataWriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberHigh;
  private final Gauge dataWriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberLow;
  private final Gauge dataWriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberHigh;
  private final Gauge dataWriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberLow;

  public DataWriterMetricsProcessor(
    DescriptionProcessorCache descriptionProcessorCache
  ) {
    checkNotNull(descriptionProcessorCache);
    this.descriptionProcessorCache = descriptionProcessorCache;
    instanceHandleHashMap = new HashMap<>();

    serializedSampleMaxSize = Gauge.build()
      .name("dds_data_writer_serialized_sample_max_size_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_writer_serialized_sample_max_size_bytes")
      .register();

    serializedSampleMinSize = Gauge.build()
      .name("dds_data_writer_serialized_sample_min_size_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_writer_serialized_sample_min_size_bytes")
      .register();

    serializedKeyMaxSize = Gauge.build()
      .name("dds_data_writer_serialized_key_max_size_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_writer_serialized_key_max_size_bytes")
      .register();

    livelinessLostStatusTotalCount = Gauge.build()
      .name("dds_data_writer_liveliness_lost_status_total_count")
      .labelNames(getLabelNames())
      .help(
        "Cumulative number of times the DataWriter failed to explicitly signal its liveliness within the liveliness period.")
      .register();

    offeredDeadlineMissedStatusTotalCount = Gauge.build()
      .name("dds_data_writer_offered_deadline_missed_status_total_count")
      .labelNames(getLabelNames())
      .help("Cumulative number of times the DataWriter failed to write within its offered deadline.")
      .register();

    offeredIncompatibleQosStatusTotalCount = Gauge.build()
      .name("dds_data_writer_offered_incompatible_qos_status_total_count")
      .labelNames(getLabelNames())
      .help(
        "Cumulative number of times the DataWriter discovered a DataReader for the same Topic with a requested QoS that is incompatible with that offered by the DataWriter.")
      .register();

    offeredIncompatibleQosStatusLastPolicyId = Gauge.build()
      .name("dds_data_writer_offered_incompatible_qos_status_last_policy_id")
      .labelNames(getLabelNames())
      .help(
        "The ID of the QosPolicy that was found to be incompatible the last time an incompatibility was detected. (Note: if there are multiple incompatible policies, only one of them is reported here.)")
      .register();

    publicationMatchedStatusTotalCount = Gauge.build()
      .name("dds_data_writer_publication_matched_status_total_count")
      .labelNames(getLabelNames())
      .help("Cumulative number of times the DataWriter discovered a \"match\" with a DataReader.")
      .register();

    publicationMatchedStatusCurrentCount = Gauge.build()
      .name("dds_data_writer_publication_matched_status_current_count")
      .labelNames(getLabelNames())
      .help("The number of DataReaders currently matched to the DataWriter.")
      .register();

    publicationMatchedStatusCurrentCountPeak = Gauge.build()
      .name("dds_data_writer_publication_matched_status_current_count_peak")
      .labelNames(getLabelNames())
      .help("The highest value that current_count has reached until now.")
      .register();

    reliableWriterCacheChangedStatusEmptyTotalCount = Gauge.build()
      .name("dds_data_writer_reliable_writer_cache_changed_status_empty_total_count")
      .labelNames(getLabelNames())
      .help("How many times the reliable DataWriter's cache of unacknowledged DDS samples has become empty.")
      .register();

    reliableWriterCacheChangedStatusFullTotalCount = Gauge.build()
      .name("dds_data_writer_reliable_writer_cache_changed_status_full_total_count")
      .labelNames(getLabelNames())
      .help("How many times the reliable DataWriter's cache of unacknowledged DDS samples has become full.")
      .register();

    reliableWriterCacheChangedStatusLowWatermarkTotalCount = Gauge.build()
      .name("dds_data_writer_reliable_writer_cache_changed_status_low_watermark_total_count")
      .labelNames(getLabelNames())
      .help(
        "How many times the reliable DataWriter's cache of unacknowledged DDS samples has fallen to the low watermark.")
      .register();

    reliableWriterCacheChangedStatusHighWatermarkTotalCount = Gauge.build()
      .name("dds_data_writer_reliable_writer_cache_changed_status_high_watermark_total_count")
      .labelNames(getLabelNames())
      .help(
        "How many times the reliable DataWriter's cache of unacknowledged DDS samples has risen to the high watermark.")
      .register();

    reliableWriterCacheChangedStatusUnacknowledgedSampleCount = Gauge.build()
      .name("dds_data_writer_reliable_writer_cache_changed_status_unacknowledged_sample_count")
      .labelNames(getLabelNames())
      .help("The current number of unacknowledged DDS samples in the DataWriter's cache.")
      .register();

    reliableWriterCacheChangedStatusUnacknowledgedSampleCountPeak = Gauge.build()
      .name("dds_data_writer_reliable_writer_cache_changed_status_unacknowledged_sample_count_peak")
      .labelNames(getLabelNames())
      .help("The highest value that unacknowledged_sample_count has reached until now.")
      .register();

    reliableReaderActivityChangedStatusActiveCount = Gauge.build()
      .name("dds_data_writer_reliable_reader_activity_changed_status_active_count")
      .labelNames(getLabelNames())
      .help("The current number of reliable readers currently matched with this reliable DataWriter.")
      .register();

    reliableReaderActivityChangedStatusInactiveCount = Gauge.build()
      .name("dds_data_writer_reliable_reader_activity_changed_status_inactive_count")
      .labelNames(getLabelNames())
      .help(
        "The number of reliable readers that have been dropped by this reliable DataWriter because they failed to send acknowledgments in a timely fashion.")
      .register();

    dataWriterCacheStatusSampleCount = Gauge.build()
      .name("dds_data_writer_cache_status_sample_count")
      .labelNames(getLabelNames())
      .help("Current number of DDS samples in the DataWriter’s queue (including DDS unregister and dispose samples)")
      .register();

    dataWriterCacheStatusSampleCountPeak = Gauge.build()
      .name("dds_data_writer_cache_status_sample_count_peak")
      .labelNames(getLabelNames())
      .help("Highest number of DDS samples in the DataWriter’s queue over the lifetime of the DataWriter.")
      .register();

    dataWriterProtocolStatusPushedSampleCount = Gauge.build()
      .name("dds_data_writer_protocol_status_pushed_sample_count")
      .labelNames(getLabelNames())
      .help("The number of user DDS samples pushed on write from a local DataWriter to a matching remote DataReader.")
      .register();

    dataWriterProtocolStatusPushedSampleBytes = Gauge.build()
      .name("dds_data_writer_protocol_status_pushed_sample_bytes")
      .labelNames(getLabelNames())
      .help(
        "The number of bytes of user DDS samples pushed on write from a local DataWriter to a matching remote DataReader.")
      .register();

    dataWriterProtocolStatusFilteredSampleCount = Gauge.build()
      .name("dds_data_writer_protocol_status_filtered_sample_count")
      .labelNames(getLabelNames())
      .help("The number of user DDS samples filtered on write from a local DataWriter to a matching remote DataReader.")
      .register();

    dataWriterProtocolStatusFilteredSampleBytes = Gauge.build()
      .name("dds_data_writer_protocol_status_filtered_sample_bytes")
      .labelNames(getLabelNames())
      .help(
        "The number of bytes of user DDS samples filtered on write from a local DataWriter to a matching remote DataReader.")
      .register();

    dataWriterProtocolStatusSentHeartbeatCount = Gauge.build()
      .name("dds_data_writer_protocol_status_sent_heartbeat_count")
      .labelNames(getLabelNames())
      .help("The number of Heartbeats sent between a local DataWriter and matching remote DataReaders.")
      .register();

    dataWriterProtocolStatusSentHeartbeatBytes = Gauge.build()
      .name("dds_data_writer_protocol_status_sent_heartbeat_bytes")
      .labelNames(getLabelNames())
      .help("The number of bytes of Heartbeats sent between a local DataWriter and matching remote DataReader.")
      .register();

    dataWriterProtocolStatusPulledSampleCount = Gauge.build()
      .name("dds_data_writer_protocol_status_pulled_sample_count")
      .labelNames(getLabelNames())
      .help("The number of user DDS samples pulled from local DataWriter by matching DataReaders.")
      .register();

    dataWriterProtocolStatusPulledSampleBytes = Gauge.build()
      .name("dds_data_writer_protocol_status_pulled_sample_bytes")
      .labelNames(getLabelNames())
      .help("The number of bytes of user DDS samples pulled from local DataWriter by matching DataReaders.")
      .register();

    dataWriterProtocolStatusReceivedAckCount = Gauge.build()
      .name("dds_data_writer_protocol_status_received_ack_count")
      .labelNames(getLabelNames())
      .help("The number of ACKs from a remote DataReader received by a local DataWriter.")
      .register();

    dataWriterProtocolStatusReceivedAckBytes = Gauge.build()
      .name("dds_data_writer_protocol_status_received_ack_bytes")
      .labelNames(getLabelNames())
      .help("The number of bytes of ACKs from a remote DataReader received by a local DataWriter.")
      .register();

    dataWriterProtocolStatusReceivedNackCount = Gauge.build()
      .name("dds_data_writer_protocol_status_received_nack_count")
      .labelNames(getLabelNames())
      .help("The number of NACKs from a remote DataReader received by a local DataWriter.")
      .register();

    dataWriterProtocolStatusReceivedNackBytes = Gauge.build()
      .name("dds_data_writer_protocol_status_received_nack_bytes")
      .labelNames(getLabelNames())
      .help("The number of bytes of NACKs from a remote DataReader received by a local DataWriter.")
      .register();

    dataWriterProtocolStatusSentGapCount = Gauge.build()
      .name("dds_data_writer_protocol_status_sent_gap_count")
      .labelNames(getLabelNames())
      .help("The number of GAPs sent from local DataWriter to matching remote DataReaders.")
      .register();

    dataWriterProtocolStatusSentGapBytes = Gauge.build()
      .name("dds_data_writer_protocol_status_sent_gap_bytes")
      .labelNames(getLabelNames())
      .help("The number of bytes of GAPs sent from local DataWriter to matching remote DataReaders.")
      .register();

    dataWriterProtocolStatusRejectedSampleCount = Gauge.build()
      .name("dds_data_writer_protocol_status_rejected_sample_count")
      .labelNames(getLabelNames())
      .help("The number of times a DDS sample is rejected for unanticipated reasons in the send path.")
      .register();

    dataWriterProtocolStatusSendWindowSize = Gauge.build()
      .name("dds_data_writer_protocol_status_send_window_size")
      .labelNames(getLabelNames())
      .help("Current maximum number of outstanding DDS samples allowed in the DataWriter's queue.")
      .register();

    dataWriterProtocolStatusFirstAvailableSequenceNumberHigh = Gauge.build()
      .name("dds_data_writer_protocol_status_first_available_sequence_number_high")
      .labelNames(getLabelNames())
      .help("Sequence number of the first available DDS sample in the DataWriter's reliability queue.")
      .register();

    dataWriterProtocolStatusFirstAvailableSequenceNumberLow = Gauge.build()
      .name("dds_data_writer_protocol_status_first_available_sequence_number_low")
      .labelNames(getLabelNames())
      .help("Sequence number of the first available DDS sample in the DataWriter's reliability queue.")
      .register();

    dataWriterProtocolStatusLastAvailableSequenceNumberHigh = Gauge.build()
      .name("dds_data_writer_protocol_status_last_available_sequence_number_high")
      .labelNames(getLabelNames())
      .help("Sequence number of the last available DDS sample in the DataWriter's reliability queue.")
      .register();

    dataWriterProtocolStatusLastAvailableSequenceNumberLow = Gauge.build()
      .name("dds_data_writer_protocol_status_last_available_sequence_number_low")
      .labelNames(getLabelNames())
      .help("Sequence number of the last available DDS sample in the DataWriter's reliability queue.")
      .register();

    dataWriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberHigh = Gauge.build()
      .name(
        "dds_data_writer_protocol_status_first_unacknowledged_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "Sequence number of the first unacknowledged DDS sample in the DataWriter's reliability queue.")
      .register();

    dataWriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberLow = Gauge.build()
      .name("dds_data_writer_protocol_status_first_unacknowledged_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help("Sequence number of the first unacknowledged DDS sample in the DataWriter's reliability queue.")
      .register();

    dataWriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberHigh = Gauge.build()
      .name(
        "dds_data_writer_protocol_status_first_available_sample_virtual_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "Virtual sequence number of the first available DDS sample in the DataWriter's reliability queue.")
      .register();

    dataWriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberLow = Gauge.build()
      .name(
        "dds_data_writer_protocol_status_first_available_sample_virtual_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "Virtual sequence number of the first available DDS sample in the DataWriter's reliability queue.")
      .register();

    dataWriterProtocolStatusLastAvailableSampleVirtualSequenceNumberHigh = Gauge.build()
      .name(
        "dds_data_writer_protocol_status_last_available_sample_virtual_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "Virtual sequence number of the last available DDS sample in the DataWriter's reliability queue.")
      .register();

    dataWriterProtocolStatusLastAvailableSampleVirtualSequenceNumberLow = Gauge.build()
      .name(
        "dds_data_writer_protocol_status_last_available_sample_virtual_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "Virtual sequence number of the last available DDS sample in the DataWriter's reliability queue.")
      .register();

    dataWriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberHigh = Gauge.build()
      .name(
        "dds_data_writer_protocol_status_first_unacknowledged_sample_virtual_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "Virtual sequence number of the first unacknowledged DDS sample in the DataWriter's reliability queue.")
      .register();

    dataWriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberLow = Gauge.build()
      .name(
        "dds_data_writer_protocol_status_first_unacknowledged_sample_virtual_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "Virtual sequence number of the first unacknowledged DDS sample in the DataWriter's reliability queue.")
      .register();

    dataWriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberHigh = Gauge.build()
      .name(
        "dds_data_writer_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "Sequence number of the first DDS sample kept in the DataWriter's queue whose keep_duration (applied when disable_positive_acks is set) has not yet elapsed.")
      .register();

    dataWriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberLow = Gauge.build()
      .name(
        "dds_data_writer_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "Sequence number of the first DDS sample kept in the DataWriter's queue whose keep_duration (applied when disable_positive_acks is set) has not yet elapsed.")
      .register();
  }

  public void process(
    DataWriterEntityStatistics sample,
    SampleInfo info
  ) {
    if ((info.instance_state == InstanceStateKind.ALIVE_INSTANCE_STATE)
      && (info.valid_data)
      && (descriptionProcessorCache.getDomainParticipantDescription(sample.participant_key) != null)
      && (descriptionProcessorCache.getPublisherDescription(sample.publisher_key) != null)
      && (descriptionProcessorCache.getDataWriterDescription(sample.datawriter_key) != null)) {
      // add / update values
      addUpdateGaugesForLabel(info.instance_handle, sample);
    } else {
      // remove values
      removeLabelsFromGauges(info.instance_handle);
    }
  }

  private void addUpdateGaugesForLabel(
    InstanceHandle_t instanceHandle,
    DataWriterEntityStatistics sample
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(
      instanceHandle,
      getLabelValues(
        descriptionProcessorCache.getDomainParticipantDescription(sample.participant_key),
        descriptionProcessorCache.getPublisherDescription(sample.publisher_key),
        descriptionProcessorCache.getDataWriterDescription(sample.datawriter_key)
      )
    );

    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(instanceHandle);

    // update gauges
    DataWriterDescription dataWriterDescription = descriptionProcessorCache
      .getDataWriterDescription(sample.datawriter_key);
    serializedSampleMaxSize.labels(labelValues).set(dataWriterDescription.serialized_sample_max_size);
    serializedSampleMinSize.labels(labelValues).set(dataWriterDescription.serialized_sample_min_size);
    serializedKeyMaxSize.labels(labelValues).set(dataWriterDescription.serialized_key_max_size);

    livelinessLostStatusTotalCount.labels(labelValues).set(
      sample.liveliness_lost_status.status.total_count);

    offeredDeadlineMissedStatusTotalCount.labels(labelValues).set(
      sample.offered_deadline_missed_status.status.total_count);

    offeredIncompatibleQosStatusTotalCount.labels(labelValues).set(
      sample.offered_incompatible_qos_status.status.total_count);

    offeredIncompatibleQosStatusLastPolicyId.labels(labelValues).set(
      sample.offered_incompatible_qos_status.status.last_policy_id);

    publicationMatchedStatusTotalCount.labels(labelValues).set(
      sample.publication_matched_status.status.total_count);

    publicationMatchedStatusCurrentCount.labels(labelValues).set(
      sample.publication_matched_status.status.current_count);

    publicationMatchedStatusCurrentCountPeak.labels(labelValues).set(
      sample.publication_matched_status.status.current_count_peak);

    reliableWriterCacheChangedStatusEmptyTotalCount.labels(labelValues).set(
      sample.reliable_writer_cache_changed_status.status.empty_reliable_writer_cache.total_count);

    reliableWriterCacheChangedStatusFullTotalCount.labels(labelValues).set(
      sample.reliable_writer_cache_changed_status.status.full_reliable_writer_cache.total_count);

    reliableWriterCacheChangedStatusLowWatermarkTotalCount.labels(labelValues).set(
      sample.reliable_writer_cache_changed_status.status.low_watermark_reliable_writer_cache.total_count);

    reliableWriterCacheChangedStatusHighWatermarkTotalCount.labels(labelValues).set(
      sample.reliable_writer_cache_changed_status.status.high_watermark_reliable_writer_cache.total_count);

    reliableWriterCacheChangedStatusUnacknowledgedSampleCount.labels(labelValues).set(
      sample.reliable_writer_cache_changed_status.status.unacknowledged_sample_count);

    reliableWriterCacheChangedStatusUnacknowledgedSampleCountPeak.labels(labelValues).set(
      sample.reliable_writer_cache_changed_status.status.unacknowledged_sample_count_peak);

    reliableReaderActivityChangedStatusActiveCount.labels(labelValues).set(
      sample.reliable_reader_activity_changed_status.status.active_count);

    reliableReaderActivityChangedStatusInactiveCount.labels(labelValues).set(
      sample.reliable_reader_activity_changed_status.status.inactive_count);

    dataWriterCacheStatusSampleCount.labels(labelValues).set(
      sample.datawriter_cache_status.status.sample_count);

    dataWriterCacheStatusSampleCountPeak.labels(labelValues).set(
      sample.datawriter_cache_status.status.sample_count_peak);

    dataWriterProtocolStatusPushedSampleCount.labels(labelValues).set(
      sample.datawriter_protocol_status.status.pushed_sample_count);

    dataWriterProtocolStatusPushedSampleBytes.labels(labelValues).set(
      sample.datawriter_protocol_status.status.pushed_sample_bytes);

    dataWriterProtocolStatusFilteredSampleCount.labels(labelValues).set(
      sample.datawriter_protocol_status.status.filtered_sample_count);

    dataWriterProtocolStatusFilteredSampleBytes.labels(labelValues).set(
      sample.datawriter_protocol_status.status.filtered_sample_bytes);

    dataWriterProtocolStatusSentHeartbeatCount.labels(labelValues).set(
      sample.datawriter_protocol_status.status.sent_heartbeat_count);

    dataWriterProtocolStatusSentHeartbeatBytes.labels(labelValues).set(
      sample.datawriter_protocol_status.status.sent_heartbeat_bytes);

    dataWriterProtocolStatusPulledSampleCount.labels(labelValues).set(
      sample.datawriter_protocol_status.status.pulled_sample_count);

    dataWriterProtocolStatusPulledSampleBytes.labels(labelValues).set(
      sample.datawriter_protocol_status.status.pulled_sample_bytes);

    dataWriterProtocolStatusReceivedAckCount.labels(labelValues).set(
      sample.datawriter_protocol_status.status.received_ack_count);

    dataWriterProtocolStatusReceivedAckBytes.labels(labelValues).set(
      sample.datawriter_protocol_status.status.received_ack_bytes);

    dataWriterProtocolStatusReceivedNackCount.labels(labelValues).set(
      sample.datawriter_protocol_status.status.received_nack_count);

    dataWriterProtocolStatusReceivedNackBytes.labels(labelValues).set(
      sample.datawriter_protocol_status.status.received_nack_bytes);

    dataWriterProtocolStatusSentGapCount.labels(labelValues).set(
      sample.datawriter_protocol_status.status.sent_gap_count);

    dataWriterProtocolStatusSentGapBytes.labels(labelValues).set(
      sample.datawriter_protocol_status.status.sent_gap_bytes);

    dataWriterProtocolStatusRejectedSampleCount.labels(labelValues).set(
      sample.datawriter_protocol_status.status.rejected_sample_count);

    dataWriterProtocolStatusSendWindowSize.labels(labelValues).set(
      sample.datawriter_protocol_status.status.send_window_size);

    dataWriterProtocolStatusFirstAvailableSequenceNumberHigh.labels(labelValues).set(
      sample.datawriter_protocol_status.status.first_available_sequence_number.high);

    dataWriterProtocolStatusFirstAvailableSequenceNumberLow.labels(labelValues).set(
      sample.datawriter_protocol_status.status.first_available_sequence_number.low);

    dataWriterProtocolStatusLastAvailableSequenceNumberHigh.labels(labelValues).set(
      sample.datawriter_protocol_status.status.last_available_sequence_number.high);

    dataWriterProtocolStatusLastAvailableSequenceNumberLow.labels(labelValues).set(
      sample.datawriter_protocol_status.status.last_available_sequence_number.low);

    dataWriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberHigh.labels(labelValues).set(
      sample.datawriter_protocol_status.status.first_unacknowledged_sample_sequence_number.high);

    dataWriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberLow.labels(labelValues).set(
      sample.datawriter_protocol_status.status.first_unacknowledged_sample_sequence_number.low);

    dataWriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberHigh.labels(labelValues).set(
      sample.datawriter_protocol_status.status.first_available_sample_virtual_sequence_number.high);

    dataWriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberLow.labels(labelValues).set(
      sample.datawriter_protocol_status.status.first_available_sample_virtual_sequence_number.low);

    dataWriterProtocolStatusLastAvailableSampleVirtualSequenceNumberHigh.labels(labelValues).set(
      sample.datawriter_protocol_status.status.last_available_sample_virtual_sequence_number.high);

    dataWriterProtocolStatusLastAvailableSampleVirtualSequenceNumberLow.labels(labelValues).set(
      sample.datawriter_protocol_status.status.last_available_sample_virtual_sequence_number.low);

    dataWriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberHigh.labels(labelValues).set(
      sample.datawriter_protocol_status.status.first_unacknowledged_sample_virtual_sequence_number.high);

    dataWriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberLow.labels(labelValues).set(
      sample.datawriter_protocol_status.status.first_unacknowledged_sample_virtual_sequence_number.low);

    dataWriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberHigh.labels(labelValues).set(
      sample.datawriter_protocol_status.status.first_unelapsed_keep_duration_sample_sequence_number.high);

    dataWriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberLow.labels(labelValues).set(
      sample.datawriter_protocol_status.status.first_unelapsed_keep_duration_sample_sequence_number.low);
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
    livelinessLostStatusTotalCount.remove(labelValues);
    offeredDeadlineMissedStatusTotalCount.remove(labelValues);
    offeredIncompatibleQosStatusTotalCount.remove(labelValues);
    offeredIncompatibleQosStatusLastPolicyId.remove(labelValues);
    publicationMatchedStatusTotalCount.remove(labelValues);
    publicationMatchedStatusCurrentCount.remove(labelValues);
    publicationMatchedStatusCurrentCountPeak.remove(labelValues);
    reliableWriterCacheChangedStatusEmptyTotalCount.remove(labelValues);
    reliableWriterCacheChangedStatusFullTotalCount.remove(labelValues);
    reliableWriterCacheChangedStatusLowWatermarkTotalCount.remove(labelValues);
    reliableWriterCacheChangedStatusHighWatermarkTotalCount.remove(labelValues);
    reliableWriterCacheChangedStatusUnacknowledgedSampleCount.remove(labelValues);
    reliableWriterCacheChangedStatusUnacknowledgedSampleCountPeak.remove(labelValues);
    reliableReaderActivityChangedStatusActiveCount.remove(labelValues);
    reliableReaderActivityChangedStatusInactiveCount.remove(labelValues);
    dataWriterCacheStatusSampleCount.remove(labelValues);
    dataWriterCacheStatusSampleCountPeak.remove(labelValues);
    dataWriterProtocolStatusPushedSampleCount.remove(labelValues);
    dataWriterProtocolStatusPushedSampleBytes.remove(labelValues);
    dataWriterProtocolStatusFilteredSampleCount.remove(labelValues);
    dataWriterProtocolStatusFilteredSampleBytes.remove(labelValues);
    dataWriterProtocolStatusSentHeartbeatCount.remove(labelValues);
    dataWriterProtocolStatusSentHeartbeatBytes.remove(labelValues);
    dataWriterProtocolStatusPulledSampleCount.remove(labelValues);
    dataWriterProtocolStatusPulledSampleBytes.remove(labelValues);
    dataWriterProtocolStatusReceivedAckCount.remove(labelValues);
    dataWriterProtocolStatusReceivedAckBytes.remove(labelValues);
    dataWriterProtocolStatusReceivedNackCount.remove(labelValues);
    dataWriterProtocolStatusReceivedNackBytes.remove(labelValues);
    dataWriterProtocolStatusSentGapCount.remove(labelValues);
    dataWriterProtocolStatusSentGapBytes.remove(labelValues);
    dataWriterProtocolStatusRejectedSampleCount.remove(labelValues);
    dataWriterProtocolStatusSendWindowSize.remove(labelValues);
    dataWriterProtocolStatusFirstAvailableSequenceNumberHigh.remove(labelValues);
    dataWriterProtocolStatusFirstAvailableSequenceNumberLow.remove(labelValues);
    dataWriterProtocolStatusLastAvailableSequenceNumberHigh.remove(labelValues);
    dataWriterProtocolStatusLastAvailableSequenceNumberLow.remove(labelValues);
    dataWriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberHigh.remove(labelValues);
    dataWriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberLow.remove(labelValues);
    dataWriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberHigh.remove(labelValues);
    dataWriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberLow.remove(labelValues);
    dataWriterProtocolStatusLastAvailableSampleVirtualSequenceNumberHigh.remove(labelValues);
    dataWriterProtocolStatusLastAvailableSampleVirtualSequenceNumberLow.remove(labelValues);
    dataWriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberHigh.remove(labelValues);
    dataWriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberLow.remove(labelValues);
    dataWriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberHigh.remove(labelValues);
    dataWriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberLow.remove(labelValues);

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
      "publisher_key",
      "publisher_name",
      "publisher_role_name",
      "datawriter_key",
      "publication_name",
      "publication_role_name",
    };
  }

  private String[] getLabelValues(
    DomainParticipantDescription domainParticipantDescription,
    PublisherDescription publisherDescription,
    DataWriterDescription dataWriterDescription
  ) {
    return new String[]{
      BuiltinTopicHelper.toString(domainParticipantDescription.entity_key.value),
      Integer.toUnsignedString(domainParticipantDescription.domain_id),
      Integer.toUnsignedString(domainParticipantDescription.host_id),
      Integer.toUnsignedString(domainParticipantDescription.process_id),
      domainParticipantDescription.qos.participant_name.name,
      domainParticipantDescription.qos.participant_name.role_name,
      BuiltinTopicHelper.toString(dataWriterDescription.topic_entity_key.value),
      dataWriterDescription.topic_name,
      dataWriterDescription.type_name,
      BuiltinTopicHelper.toString(publisherDescription.entity_key.value),
      publisherDescription.qos.publisher_name.name,
      publisherDescription.qos.publisher_name.role_name,
      BuiltinTopicHelper.toString(dataWriterDescription.entity_key.value),
      dataWriterDescription.qos.publication_name.name,
      dataWriterDescription.qos.publication_name.role_name,
    };
  }
}
