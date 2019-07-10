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

import com.github.aguther.dds.util.BuiltinTopicHelper;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.rti.dds.monitoring.DataWriterEntityMatchedSubscriptionWithLocatorStatistics;
import io.prometheus.client.Gauge;
import java.util.HashMap;

public class DataWriterEntityMatchedSubscriptionWithLocatorStatisticsMetricProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

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

  public DataWriterEntityMatchedSubscriptionWithLocatorStatisticsMetricProcessor() {
    instanceHandleHashMap = new HashMap<>();

    datawriterProtocolStatusPushedSampleCount = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_pushed_sample_count")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_pushed_sample_count")
        .register();

    datawriterProtocolStatusPushedSampleBytes = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_pushed_sample_bytes")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_pushed_sample_bytes")
        .register();

    datawriterProtocolStatusFilteredSampleCount = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_filtered_sample_count")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_filtered_sample_count")
        .register();

    datawriterProtocolStatusFilteredSampleBytes = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_filtered_sample_bytes")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_filtered_sample_bytes")
        .register();

    datawriterProtocolStatusSentHeartbeatCount = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_sent_heartbeat_count")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_sent_heartbeat_count")
        .register();

    datawriterProtocolStatusSentHeartbeatBytes = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_sent_heartbeat_bytes")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_sent_heartbeat_bytes")
        .register();

    datawriterProtocolStatusPulledSampleCount = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_pulled_sample_count")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_pulled_sample_count")
        .register();

    datawriterProtocolStatusPulledSampleBytes = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_pulled_sample_bytes")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_pulled_sample_bytes")
        .register();

    datawriterProtocolStatusReceivedAckCount = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_received_ack_count")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_received_ack_count")
        .register();

    datawriterProtocolStatusReceivedAckBytes = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_received_ack_bytes")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_received_ack_bytes")
        .register();

    datawriterProtocolStatusReceivedNackCount = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_received_nack_count")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_received_nack_count")
        .register();

    datawriterProtocolStatusReceivedNackBytes = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_received_nack_bytes")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_received_nack_bytes")
        .register();

    datawriterProtocolStatusSentGapCount = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_sent_gap_count")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_sent_gap_count")
        .register();

    datawriterProtocolStatusSentGapBytes = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_sent_gap_bytes")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_sent_gap_bytes")
        .register();

    datawriterProtocolStatusRejectedSampleCount = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_rejected_sample_count")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_rejected_sample_count")
        .register();

    datawriterProtocolStatusSendWindowSize = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_send_window_size")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_send_window_size")
        .register();

    datawriterProtocolStatusFirstAvailableSequenceNumberHigh = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_available_sequence_number_high")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_available_sequence_number_high")
        .register();

    datawriterProtocolStatusFirstAvailableSequenceNumberLow = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_available_sequence_number_low")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_available_sequence_number_low")
        .register();

    datawriterProtocolStatusLastAvailableSequenceNumberHigh = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_last_available_sequence_number_high")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_last_available_sequence_number_high")
        .register();

    datawriterProtocolStatusLastAvailableSequenceNumberLow = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_last_available_sequence_number_low")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_last_available_sequence_number_low")
        .register();

    datawriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberHigh = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_unacknowledged_sample_sequence_number_high")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_unacknowledged_sample_sequence_number_high")
        .register();

    datawriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberLow = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_unacknowledged_sample_sequence_number_low")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_unacknowledged_sample_sequence_number_low")
        .register();

    datawriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberHigh = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_available_sample_virtual_sequence_number_high")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_available_sample_virtual_sequence_number_high")
        .register();

    datawriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberLow = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_available_sample_virtual_sequence_number_low")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_available_sample_virtual_sequence_number_low")
        .register();

    datawriterProtocolStatusLastAvailableSampleVirtualSequenceNumberHigh = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_last_available_sample_virtual_sequence_number_high")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_last_available_sample_virtual_sequence_number_high")
        .register();

    datawriterProtocolStatusLastAvailableSampleVirtualSequenceNumberLow = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_last_available_sample_virtual_sequence_number_low")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_last_available_sample_virtual_sequence_number_low")
        .register();

    datawriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberHigh = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_unacknowledged_sample_virtual_sequence_number_high")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_unacknowledged_sample_virtual_sequence_number_high")
        .register();

    datawriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberLow = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_unacknowledged_sample_virtual_sequence_number_low")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_unacknowledged_sample_virtual_sequence_number_low")
        .register();

    datawriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberHigh = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_high")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_high")
        .register();

    datawriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberLow = Gauge.build()
        .name(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_low")
        .labelNames(getLabelNames())
        .help(
            "datawriter_entity_matched_subscription_with_locator_statistics_datawriter_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_low")
        .register();
  }

  public void process(
      DataWriterEntityMatchedSubscriptionWithLocatorStatistics sample,
      SampleInfo info
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(info.instance_handle, getLabelValues(sample));
    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(info.instance_handle);

    // check if sample is alive and contains valid data
    if (info.instance_state != InstanceStateKind.ALIVE_INSTANCE_STATE || !info.valid_data) {
      // remove labels
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
        "subscription_locator_kind",
        "subscription_locator_address",
        "subscription_locator_port",
        "period"
    };
  }

  private String[] getLabelValues(
      DataWriterEntityMatchedSubscriptionWithLocatorStatistics sample
  ) {
    return new String[]{
        BuiltinTopicHelper.toString(sample.datawriter_key.value),
        Integer.toUnsignedString(sample.subscription_locator.kind),
        BuiltinTopicHelper.toString(sample.subscription_locator.address),
        Integer.toUnsignedString(sample.subscription_locator._port),
        Long.toUnsignedString((long) sample.period.sec * 1000000000 + (long) sample.period.nanosec)
    };
  }
}
