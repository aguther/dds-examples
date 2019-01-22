package com.github.aguther.dds.examples.prometheus.monitoring;

import com.github.aguther.dds.util.BuiltinTopicHelper;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.rti.dds.monitoring.DataWriterEntityStatistics;
import io.prometheus.client.Gauge;
import java.util.HashMap;

public class DataWriterEntityStatisticsMetricProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

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
  private final Gauge datawriterCacheStatusSampleCount;
  private final Gauge datawriterCacheStatusSampleCountPeak;
  private final Gauge datawriterProtocolStatusPushedSampleCount;
  private final Gauge datawriterProtocolStatusPushedSampleBytes;
  private final Gauge datawriterProtocolStatusFilteredSampleCount;
  private final Gauge datawriterProtocolStatusFilteredSampleBytes;
  private final Gauge datawriterProtocolStatusSentHeartbeatCount;
  private final Gauge datawriterProtocolStatusSentHeartbeatBytes;
  private final Gauge datawriterProtocolStatusPulledSampleCount;
  private final Gauge datawriterProtocolStatusPulledSampleBytes;
  private final Gauge datawriterProtocolStatusReceivedAckCount;
  private final Gauge datawriterProtocolStatusReceivedAckBytes;
  private final Gauge datawriterProtocolStatusReceivedNackCount;
  private final Gauge datawriterProtocolStatusReceivedNackBytes;
  private final Gauge datawriterProtocolStatusSentGapCount;
  private final Gauge datawriterProtocolStatusSentGapBytes;
  private final Gauge datawriterProtocolStatusRejectedSampleCount;
  private final Gauge datawriterProtocolStatusSendWindowSize;
  private final Gauge datawriterProtocolStatusFirstAvailableSequenceNumberHigh;
  private final Gauge datawriterProtocolStatusFirstAvailableSequenceNumberLow;
  private final Gauge datawriterProtocolStatusLastAvailableSequenceNumberHigh;
  private final Gauge datawriterProtocolStatusLastAvailableSequenceNumberLow;
  private final Gauge datawriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberHigh;
  private final Gauge datawriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberLow;
  private final Gauge datawriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberHigh;
  private final Gauge datawriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberLow;
  private final Gauge datawriterProtocolStatusLastAvailableSampleVirtualSequenceNumberHigh;
  private final Gauge datawriterProtocolStatusLastAvailableSampleVirtualSequenceNumberLow;
  private final Gauge datawriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberHigh;
  private final Gauge datawriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberLow;
  private final Gauge datawriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberHigh;
  private final Gauge datawriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberLow;

  public DataWriterEntityStatisticsMetricProcessor() {
    instanceHandleHashMap = new HashMap<>();

    livelinessLostStatusTotalCount = Gauge.build()
        .name("datawriter_entity_statistics_liveliness_lost_status_total_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_liveliness_lost_status_total_count")
        .register();

    offeredDeadlineMissedStatusTotalCount = Gauge.build()
        .name("datawriter_entity_statistics_offered_deadline_missed_status_total_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_offered_deadline_missed_status_total_count")
        .register();

    offeredIncompatibleQosStatusTotalCount = Gauge.build()
        .name("datawriter_entity_statistics_offered_incompatible_qos_status_total_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_offered_incompatible_qos_status_total_count")
        .register();

    offeredIncompatibleQosStatusLastPolicyId = Gauge.build()
        .name("datawriter_entity_statistics_offered_incompatible_qos_status_last_policy_id")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_offered_incompatible_qos_status_last_policy_id")
        .register();

    publicationMatchedStatusTotalCount = Gauge.build()
        .name("datawriter_entity_statistics_publication_matched_status_total_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_publication_matched_status_total_count")
        .register();

    publicationMatchedStatusCurrentCount = Gauge.build()
        .name("datawriter_entity_statistics_publication_matched_status_current_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_publication_matched_status_current_count")
        .register();

    publicationMatchedStatusCurrentCountPeak = Gauge.build()
        .name("datawriter_entity_statistics_publication_matched_status_current_count_peak")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_publication_matched_status_current_count_peak")
        .register();

    reliableWriterCacheChangedStatusEmptyTotalCount = Gauge.build()
        .name("datawriter_entity_statistics_reliable_writer_cache_changed_status_empty_total_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_reliable_writer_cache_changed_status_empty_total_count")
        .register();

    reliableWriterCacheChangedStatusFullTotalCount = Gauge.build()
        .name("datawriter_entity_statistics_reliable_writer_cache_changed_status_full_total_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_reliable_writer_cache_changed_status_full_total_count")
        .register();

    reliableWriterCacheChangedStatusLowWatermarkTotalCount = Gauge.build()
        .name("datawriter_entity_statistics_reliable_writer_cache_changed_status_low_watermark_total_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_reliable_writer_cache_changed_status_low_watermark_total_count")
        .register();

    reliableWriterCacheChangedStatusHighWatermarkTotalCount = Gauge.build()
        .name("datawriter_entity_statistics_reliable_writer_cache_changed_status_high_watermark_total_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_reliable_writer_cache_changed_status_high_watermark_total_count")
        .register();

    reliableWriterCacheChangedStatusUnacknowledgedSampleCount = Gauge.build()
        .name("datawriter_entity_statistics_reliable_writer_cache_changed_status_unacknowledged_sample_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_reliable_writer_cache_changed_status_unacknowledged_sample_count")
        .register();

    reliableWriterCacheChangedStatusUnacknowledgedSampleCountPeak = Gauge.build()
        .name("datawriter_entity_statistics_reliable_writer_cache_changed_status_unacknowledged_sample_count_peak")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_reliable_writer_cache_changed_status_unacknowledged_sample_count_peak")
        .register();

    reliableReaderActivityChangedStatusActiveCount = Gauge.build()
        .name("datawriter_entity_statistics_reliable_reader_activity_changed_status_active_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_reliable_reader_activity_changed_status_active_count")
        .register();

    reliableReaderActivityChangedStatusInactiveCount = Gauge.build()
        .name("datawriter_entity_statistics_reliable_reader_activity_changed_status_inactive_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_reliable_reader_activity_changed_status_inactive_count")
        .register();

    datawriterCacheStatusSampleCount = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_cache_status_sample_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_cache_status_sample_count")
        .register();

    datawriterCacheStatusSampleCountPeak = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_cache_status_sample_count_peak")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_cache_status_sample_count_peak")
        .register();

    datawriterProtocolStatusPushedSampleCount = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_pushed_sample_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_pushed_sample_count")
        .register();

    datawriterProtocolStatusPushedSampleBytes = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_pushed_sample_bytes")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_pushed_sample_bytes")
        .register();

    datawriterProtocolStatusFilteredSampleCount = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_filtered_sample_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_filtered_sample_count")
        .register();

    datawriterProtocolStatusFilteredSampleBytes = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_filtered_sample_bytes")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_filtered_sample_bytes")
        .register();

    datawriterProtocolStatusSentHeartbeatCount = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_sent_heartbeat_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_sent_heartbeat_count")
        .register();

    datawriterProtocolStatusSentHeartbeatBytes = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_sent_heartbeat_bytes")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_sent_heartbeat_bytes")
        .register();

    datawriterProtocolStatusPulledSampleCount = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_pulled_sample_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_pulled_sample_count")
        .register();

    datawriterProtocolStatusPulledSampleBytes = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_pulled_sample_bytes")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_pulled_sample_bytes")
        .register();

    datawriterProtocolStatusReceivedAckCount = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_received_ack_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_received_ack_count")
        .register();

    datawriterProtocolStatusReceivedAckBytes = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_received_ack_bytes")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_received_ack_bytes")
        .register();

    datawriterProtocolStatusReceivedNackCount = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_received_nack_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_received_nack_count")
        .register();

    datawriterProtocolStatusReceivedNackBytes = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_received_nack_bytes")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_received_nack_bytes")
        .register();

    datawriterProtocolStatusSentGapCount = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_sent_gap_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_sent_gap_count")
        .register();

    datawriterProtocolStatusSentGapBytes = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_sent_gap_bytes")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_sent_gap_bytes")
        .register();

    datawriterProtocolStatusRejectedSampleCount = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_rejected_sample_count")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_rejected_sample_count")
        .register();

    datawriterProtocolStatusSendWindowSize = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_send_window_size")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_send_window_size")
        .register();

    datawriterProtocolStatusFirstAvailableSequenceNumberHigh = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_first_available_sequence_number_high")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_first_available_sequence_number_high")
        .register();

    datawriterProtocolStatusFirstAvailableSequenceNumberLow = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_first_available_sequence_number_low")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_first_available_sequence_number_low")
        .register();

    datawriterProtocolStatusLastAvailableSequenceNumberHigh = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_last_available_sequence_number_high")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_last_available_sequence_number_high")
        .register();

    datawriterProtocolStatusLastAvailableSequenceNumberLow = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_last_available_sequence_number_low")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_last_available_sequence_number_low")
        .register();

    datawriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberHigh = Gauge.build()
        .name(
            "datawriter_entity_statistics_datawriter_protocol_status_first_unacknowledged_sample_sequence_number_high")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_statistics_datawriter_protocol_status_first_unacknowledged_sample_sequence_number_high")
        .register();

    datawriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberLow = Gauge.build()
        .name("datawriter_entity_statistics_datawriter_protocol_status_first_unacknowledged_sample_sequence_number_low")
        .labelNames(getLabelNames())
        .help("datawriter_entity_statistics_datawriter_protocol_status_first_unacknowledged_sample_sequence_number_low")
        .register();

    datawriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberHigh = Gauge.build()
        .name(
            "datawriter_entity_statistics_datawriter_protocol_status_first_available_sample_virtual_sequence_number_high")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_statistics_datawriter_protocol_status_first_available_sample_virtual_sequence_number_high")
        .register();

    datawriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberLow = Gauge.build()
        .name(
            "datawriter_entity_statistics_datawriter_protocol_status_first_available_sample_virtual_sequence_number_low")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_statistics_datawriter_protocol_status_first_available_sample_virtual_sequence_number_low")
        .register();

    datawriterProtocolStatusLastAvailableSampleVirtualSequenceNumberHigh = Gauge.build()
        .name(
            "datawriter_entity_statistics_datawriter_protocol_status_last_available_sample_virtual_sequence_number_high")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_statistics_datawriter_protocol_status_last_available_sample_virtual_sequence_number_high")
        .register();

    datawriterProtocolStatusLastAvailableSampleVirtualSequenceNumberLow = Gauge.build()
        .name(
            "datawriter_entity_statistics_datawriter_protocol_status_last_available_sample_virtual_sequence_number_low")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_statistics_datawriter_protocol_status_last_available_sample_virtual_sequence_number_low")
        .register();

    datawriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberHigh = Gauge.build()
        .name(
            "datawriter_entity_statistics_datawriter_protocol_status_first_unacknowledged_sample_virtual_sequence_number_high")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_statistics_datawriter_protocol_status_first_unacknowledged_sample_virtual_sequence_number_high")
        .register();

    datawriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberLow = Gauge.build()
        .name(
            "datawriter_entity_statistics_datawriter_protocol_status_first_unacknowledged_sample_virtual_sequence_number_low")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_statistics_datawriter_protocol_status_first_unacknowledged_sample_virtual_sequence_number_low")
        .register();

    datawriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberHigh = Gauge.build()
        .name(
            "datawriter_entity_statistics_datawriter_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_high")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_statistics_datawriter_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_high")
        .register();

    datawriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberLow = Gauge.build()
        .name(
            "datawriter_entity_statistics_datawriter_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_low")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_statistics_datawriter_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_low")
        .register();
  }

  public void process(
      DataWriterEntityStatistics sample,
      SampleInfo info
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(info.instance_handle, getLabelValues(sample));
    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(info.instance_handle);

    // check if sample is alive and contains valid data
    if (info.instance_state != InstanceStateKind.ALIVE_INSTANCE_STATE || !info.valid_data) {
      // remove labels
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
      datawriterCacheStatusSampleCount.remove(labelValues);
      datawriterCacheStatusSampleCountPeak.remove(labelValues);
      datawriterProtocolStatusPushedSampleCount.remove(labelValues);
      datawriterProtocolStatusPushedSampleBytes.remove(labelValues);
      datawriterProtocolStatusFilteredSampleCount.remove(labelValues);
      datawriterProtocolStatusFilteredSampleBytes.remove(labelValues);
      datawriterProtocolStatusSentHeartbeatCount.remove(labelValues);
      datawriterProtocolStatusSentHeartbeatBytes.remove(labelValues);
      datawriterProtocolStatusPulledSampleCount.remove(labelValues);
      datawriterProtocolStatusPulledSampleBytes.remove(labelValues);
      datawriterProtocolStatusReceivedAckCount.remove(labelValues);
      datawriterProtocolStatusReceivedAckBytes.remove(labelValues);
      datawriterProtocolStatusReceivedNackCount.remove(labelValues);
      datawriterProtocolStatusReceivedNackBytes.remove(labelValues);
      datawriterProtocolStatusSentGapCount.remove(labelValues);
      datawriterProtocolStatusSentGapBytes.remove(labelValues);
      datawriterProtocolStatusRejectedSampleCount.remove(labelValues);
      datawriterProtocolStatusSendWindowSize.remove(labelValues);
      datawriterProtocolStatusFirstAvailableSequenceNumberHigh.remove(labelValues);
      datawriterProtocolStatusFirstAvailableSequenceNumberLow.remove(labelValues);
      datawriterProtocolStatusLastAvailableSequenceNumberHigh.remove(labelValues);
      datawriterProtocolStatusLastAvailableSequenceNumberLow.remove(labelValues);
      datawriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberHigh.remove(labelValues);
      datawriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberLow.remove(labelValues);
      datawriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberHigh.remove(labelValues);
      datawriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberLow.remove(labelValues);
      datawriterProtocolStatusLastAvailableSampleVirtualSequenceNumberHigh.remove(labelValues);
      datawriterProtocolStatusLastAvailableSampleVirtualSequenceNumberLow.remove(labelValues);
      datawriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberHigh.remove(labelValues);
      datawriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberLow.remove(labelValues);
      datawriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberHigh.remove(labelValues);
      datawriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberLow.remove(labelValues);
      // remove instance from hash map
      instanceHandleHashMap.remove(info.instance_handle);
      return;
    }

    // update gauges
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

    datawriterCacheStatusSampleCount.labels(labelValues).set(
        sample.datawriter_cache_status.status.sample_count);

    datawriterCacheStatusSampleCountPeak.labels(labelValues).set(
        sample.datawriter_cache_status.status.sample_count_peak);

    datawriterProtocolStatusPushedSampleCount.labels(labelValues).set(
        sample.datawriter_protocol_status.status.pushed_sample_count);

    datawriterProtocolStatusPushedSampleBytes.labels(labelValues).set(
        sample.datawriter_protocol_status.status.pushed_sample_bytes);

    datawriterProtocolStatusFilteredSampleCount.labels(labelValues).set(
        sample.datawriter_protocol_status.status.filtered_sample_count);

    datawriterProtocolStatusFilteredSampleBytes.labels(labelValues).set(
        sample.datawriter_protocol_status.status.filtered_sample_bytes);

    datawriterProtocolStatusSentHeartbeatCount.labels(labelValues).set(
        sample.datawriter_protocol_status.status.sent_heartbeat_count);

    datawriterProtocolStatusSentHeartbeatBytes.labels(labelValues).set(
        sample.datawriter_protocol_status.status.sent_heartbeat_bytes);

    datawriterProtocolStatusPulledSampleCount.labels(labelValues).set(
        sample.datawriter_protocol_status.status.pulled_sample_count);

    datawriterProtocolStatusPulledSampleBytes.labels(labelValues).set(
        sample.datawriter_protocol_status.status.pulled_sample_bytes);

    datawriterProtocolStatusReceivedAckCount.labels(labelValues).set(
        sample.datawriter_protocol_status.status.received_ack_count);

    datawriterProtocolStatusReceivedAckBytes.labels(labelValues).set(
        sample.datawriter_protocol_status.status.received_ack_bytes);

    datawriterProtocolStatusReceivedNackCount.labels(labelValues).set(
        sample.datawriter_protocol_status.status.received_nack_count);

    datawriterProtocolStatusReceivedNackBytes.labels(labelValues).set(
        sample.datawriter_protocol_status.status.received_nack_bytes);

    datawriterProtocolStatusSentGapCount.labels(labelValues).set(
        sample.datawriter_protocol_status.status.sent_gap_count);

    datawriterProtocolStatusSentGapBytes.labels(labelValues).set(
        sample.datawriter_protocol_status.status.sent_gap_bytes);

    datawriterProtocolStatusRejectedSampleCount.labels(labelValues).set(
        sample.datawriter_protocol_status.status.rejected_sample_count);

    datawriterProtocolStatusSendWindowSize.labels(labelValues).set(
        sample.datawriter_protocol_status.status.send_window_size);

    datawriterProtocolStatusFirstAvailableSequenceNumberHigh.labels(labelValues).set(
        sample.datawriter_protocol_status.status.first_available_sequence_number.high);

    datawriterProtocolStatusFirstAvailableSequenceNumberLow.labels(labelValues).set(
        sample.datawriter_protocol_status.status.first_available_sequence_number.low);

    datawriterProtocolStatusLastAvailableSequenceNumberHigh.labels(labelValues).set(
        sample.datawriter_protocol_status.status.last_available_sequence_number.high);

    datawriterProtocolStatusLastAvailableSequenceNumberLow.labels(labelValues).set(
        sample.datawriter_protocol_status.status.last_available_sequence_number.low);

    datawriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberHigh.labels(labelValues).set(
        sample.datawriter_protocol_status.status.first_unacknowledged_sample_sequence_number.high);

    datawriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberLow.labels(labelValues).set(
        sample.datawriter_protocol_status.status.first_unacknowledged_sample_sequence_number.low);

    datawriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberHigh.labels(labelValues).set(
        sample.datawriter_protocol_status.status.first_available_sample_virtual_sequence_number.high);

    datawriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberLow.labels(labelValues).set(
        sample.datawriter_protocol_status.status.first_available_sample_virtual_sequence_number.low);

    datawriterProtocolStatusLastAvailableSampleVirtualSequenceNumberHigh.labels(labelValues).set(
        sample.datawriter_protocol_status.status.last_available_sample_virtual_sequence_number.high);

    datawriterProtocolStatusLastAvailableSampleVirtualSequenceNumberLow.labels(labelValues).set(
        sample.datawriter_protocol_status.status.last_available_sample_virtual_sequence_number.low);

    datawriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberHigh.labels(labelValues).set(
        sample.datawriter_protocol_status.status.first_unacknowledged_sample_virtual_sequence_number.high);

    datawriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberLow.labels(labelValues).set(
        sample.datawriter_protocol_status.status.first_unacknowledged_sample_virtual_sequence_number.low);

    datawriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberHigh.labels(labelValues).set(
        sample.datawriter_protocol_status.status.first_unelapsed_keep_duration_sample_sequence_number.high);

    datawriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberLow.labels(labelValues).set(
        sample.datawriter_protocol_status.status.first_unelapsed_keep_duration_sample_sequence_number.low);
  }

  private String[] getLabelNames() {
    return new String[]{
        "datawriter_key",
        "period",
        "participant_key",
        "publisher_key",
        "topic_key",
        "topic_name",
        "domain_id",
        "host_id",
        "process_id",
    };
  }

  private String[] getLabelValues(
      DataWriterEntityStatistics sample
  ) {
    return new String[]{
        BuiltinTopicHelper.toString(sample.datawriter_key.value),
        Long.toUnsignedString((long) sample.period.sec * 1000000000 + (long) sample.period.nanosec),
        BuiltinTopicHelper.toString(sample.participant_key.value),
        BuiltinTopicHelper.toString(sample.publisher_key.value),
        BuiltinTopicHelper.toString(sample.topic_key.value),
        sample.topic_name,
        Integer.toUnsignedString(sample.domain_id),
        Integer.toUnsignedString(sample.host_id),
        Integer.toUnsignedString(sample.process_id),
    };
  }
}
