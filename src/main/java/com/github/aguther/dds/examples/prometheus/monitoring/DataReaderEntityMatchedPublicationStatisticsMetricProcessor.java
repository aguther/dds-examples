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
import idl.rti.dds.monitoring.DataReaderEntityMatchedPublicationStatistics;
import io.prometheus.client.Gauge;
import java.util.HashMap;

public class DataReaderEntityMatchedPublicationStatisticsMetricProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

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

  public DataReaderEntityMatchedPublicationStatisticsMetricProcessor() {
    instanceHandleHashMap = new HashMap<>();

    datareaderProtocolStatusReceivedSampleCount = Gauge.build()
      .name("datareader_entity_matched_publication_statistics_datareader_protocol_status_received_sample_count")
      .labelNames(getLabelNames())
      .help("datareader_entity_matched_publication_statistics_datareader_protocol_status_received_sample_count")
      .register();

    datareaderProtocolStatusReceivedSampleBytes = Gauge.build()
      .name("datareader_entity_matched_publication_statistics_datareader_protocol_status_received_sample_bytes")
      .labelNames(getLabelNames())
      .help("datareader_entity_matched_publication_statistics_datareader_protocol_status_received_sample_bytes")
      .register();

    datareaderProtocolStatusDuplicateSampleCount = Gauge.build()
      .name("datareader_entity_matched_publication_statistics_datareader_protocol_status_duplicate_sample_count")
      .labelNames(getLabelNames())
      .help("datareader_entity_matched_publication_statistics_datareader_protocol_status_duplicate_sample_count")
      .register();

    datareaderProtocolStatusDuplicateSampleBytes = Gauge.build()
      .name("datareader_entity_matched_publication_statistics_datareader_protocol_status_duplicate_sample_bytes")
      .labelNames(getLabelNames())
      .help("datareader_entity_matched_publication_statistics_datareader_protocol_status_duplicate_sample_bytes")
      .register();

    datareaderProtocolStatusFilteredSampleCount = Gauge.build()
      .name("datareader_entity_matched_publication_statistics_datareader_protocol_status_filtered_sample_count")
      .labelNames(getLabelNames())
      .help("datareader_entity_matched_publication_statistics_datareader_protocol_status_filtered_sample_count")
      .register();

    datareaderProtocolStatusFilteredSampleBytes = Gauge.build()
      .name("datareader_entity_matched_publication_statistics_datareader_protocol_status_filtered_sample_bytes")
      .labelNames(getLabelNames())
      .help("datareader_entity_matched_publication_statistics_datareader_protocol_status_filtered_sample_bytes")
      .register();

    datareaderProtocolStatusReceivedHeartbeatCount = Gauge.build()
      .name("datareader_entity_matched_publication_statistics_datareader_protocol_status_received_heartbeat_count")
      .labelNames(getLabelNames())
      .help("datareader_entity_matched_publication_statistics_datareader_protocol_status_received_heartbeat_count")
      .register();

    datareaderProtocolStatusReceivedHeartbeatBytes = Gauge.build()
      .name("datareader_entity_matched_publication_statistics_datareader_protocol_status_received_heartbeat_bytes")
      .labelNames(getLabelNames())
      .help("datareader_entity_matched_publication_statistics_datareader_protocol_status_received_heartbeat_bytes")
      .register();

    datareaderProtocolStatusSentAckCount = Gauge.build()
      .name("datareader_entity_matched_publication_statistics_datareader_protocol_status_sent_ack_count")
      .labelNames(getLabelNames())
      .help("datareader_entity_matched_publication_statistics_datareader_protocol_status_sent_ack_count")
      .register();

    datareaderProtocolStatusSentAckBytes = Gauge.build()
      .name("datareader_entity_matched_publication_statistics_datareader_protocol_status_sent_ack_bytes")
      .labelNames(getLabelNames())
      .help("datareader_entity_matched_publication_statistics_datareader_protocol_status_sent_ack_bytes")
      .register();

    datareaderProtocolStatusSentNackCount = Gauge.build()
      .name("datareader_entity_matched_publication_statistics_datareader_protocol_status_sent_nack_count")
      .labelNames(getLabelNames())
      .help("datareader_entity_matched_publication_statistics_datareader_protocol_status_sent_nack_count")
      .register();

    datareaderProtocolStatusSentNackBytes = Gauge.build()
      .name("datareader_entity_matched_publication_statistics_datareader_protocol_status_sent_nack_bytes")
      .labelNames(getLabelNames())
      .help("datareader_entity_matched_publication_statistics_datareader_protocol_status_sent_nack_bytes")
      .register();

    datareaderProtocolStatusReceivedGapCount = Gauge.build()
      .name("datareader_entity_matched_publication_statistics_datareader_protocol_status_received_gap_count")
      .labelNames(getLabelNames())
      .help("datareader_entity_matched_publication_statistics_datareader_protocol_status_received_gap_count")
      .register();

    datareaderProtocolStatusReceivedGapBytes = Gauge.build()
      .name("datareader_entity_matched_publication_statistics_datareader_protocol_status_received_gap_bytes")
      .labelNames(getLabelNames())
      .help("datareader_entity_matched_publication_statistics_datareader_protocol_status_received_gap_bytes")
      .register();

    datareaderProtocolStatusRejectedSampleCount = Gauge.build()
      .name("datareader_entity_matched_publication_statistics_datareader_protocol_status_rejected_sample_count")
      .labelNames(getLabelNames())
      .help("datareader_entity_matched_publication_statistics_datareader_protocol_status_rejected_sample_count")
      .register();

    datareaderProtocolStatusFirstAvailableSampleSequenceNumberHigh = Gauge.build()
      .name(
        "datareader_entity_matched_publication_statistics_datareader_protocol_status_first_available_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "datareader_entity_matched_publication_statistics_datareader_protocol_status_first_available_sample_sequence_number_high")
      .register();

    datareaderProtocolStatusFirstAvailableSampleSequenceNumberLow = Gauge.build()
      .name(
        "datareader_entity_matched_publication_statistics_datareader_protocol_status_first_available_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "datareader_entity_matched_publication_statistics_datareader_protocol_status_first_available_sample_sequence_number_low")
      .register();

    datareaderProtocolStatusLastAvailableSampleSequenceNumberHigh = Gauge.build()
      .name(
        "datareader_entity_matched_publication_statistics_datareader_protocol_status_last_available_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "datareader_entity_matched_publication_statistics_datareader_protocol_status_last_available_sample_sequence_number_high")
      .register();

    datareaderProtocolStatusLastAvailableSampleSequenceNumberLow = Gauge.build()
      .name(
        "datareader_entity_matched_publication_statistics_datareader_protocol_status_last_available_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "datareader_entity_matched_publication_statistics_datareader_protocol_status_last_available_sample_sequence_number_low")
      .register();

    datareaderProtocolStatusLastCommittedSampleSequenceNumberHigh = Gauge.build()
      .name(
        "datareader_entity_matched_publication_statistics_datareader_protocol_status_last_committed_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "datareader_entity_matched_publication_statistics_datareader_protocol_status_last_committed_sample_sequence_number_high")
      .register();

    datareaderProtocolStatusLastCommittedSampleSequenceNumberLow = Gauge.build()
      .name(
        "datareader_entity_matched_publication_statistics_datareader_protocol_status_last_committed_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "datareader_entity_matched_publication_statistics_datareader_protocol_status_last_committed_sample_sequence_number_low")
      .register();

    datareaderProtocolStatusUncommittedSampleCount = Gauge.build()
      .name("datareader_entity_matched_publication_statistics_datareader_protocol_status_uncommitted_sample_count")
      .labelNames(getLabelNames())
      .help("datareader_entity_matched_publication_statistics_datareader_protocol_status_uncommitted_sample_count")
      .register();
  }

  public void process(
    DataReaderEntityMatchedPublicationStatistics sample,
    SampleInfo info
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(info.instance_handle, getLabelValues(sample));
    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(info.instance_handle);

    // check if sample is alive and contains valid data
    if (info.instance_state != InstanceStateKind.ALIVE_INSTANCE_STATE || !info.valid_data) {
      // remove labels
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
      instanceHandleHashMap.remove(info.instance_handle);
      return;
    }

    // update gauges
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

  private String[] getLabelNames() {
    return new String[]{
      "datareader_key",
      "publication_handle",
      "period",
    };
  }

  private String[] getLabelValues(
    DataReaderEntityMatchedPublicationStatistics sample
  ) {
    return new String[]{
      BuiltinTopicHelper.toString(sample.datareader_key.value),
      BuiltinTopicHelper.toString(sample.publication_handle.value),
      Long.toUnsignedString((long) sample.period.sec * 1000000000 + (long) sample.period.nanosec),
    };
  }
}
