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
import idl.rti.dds.monitoring.DataReaderEntityMatchedPublicationStatistics;
import idl.rti.dds.monitoring.DomainParticipantDescription;
import idl.rti.dds.monitoring.SubscriberDescription;
import io.prometheus.client.Gauge;
import java.util.HashMap;

public class DataReaderMatchedPublicationMetricsProcessor {

  private final DescriptionProcessorCache descriptionProcessorCache;
  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

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

  public DataReaderMatchedPublicationMetricsProcessor(
    DescriptionProcessorCache descriptionProcessorCache
  ) {
    checkNotNull(descriptionProcessorCache);
    this.descriptionProcessorCache = descriptionProcessorCache;
    instanceHandleHashMap = new HashMap<>();

    dataReaderProtocolStatusReceivedSampleCount = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_received_sample_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_matched_publication_protocol_status_received_sample_count")
      .register();

    dataReaderProtocolStatusReceivedSampleBytes = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_received_sample_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_matched_publication_protocol_status_received_sample_bytes")
      .register();

    dataReaderProtocolStatusDuplicateSampleCount = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_duplicate_sample_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_matched_publication_protocol_status_duplicate_sample_count")
      .register();

    dataReaderProtocolStatusDuplicateSampleBytes = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_duplicate_sample_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_matched_publication_protocol_status_duplicate_sample_bytes")
      .register();

    dataReaderProtocolStatusFilteredSampleCount = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_filtered_sample_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_matched_publication_protocol_status_filtered_sample_count")
      .register();

    dataReaderProtocolStatusFilteredSampleBytes = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_filtered_sample_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_matched_publication_protocol_status_filtered_sample_bytes")
      .register();

    dataReaderProtocolStatusReceivedHeartbeatCount = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_received_heartbeat_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_matched_publication_protocol_status_received_heartbeat_count")
      .register();

    dataReaderProtocolStatusReceivedHeartbeatBytes = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_received_heartbeat_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_matched_publication_protocol_status_received_heartbeat_bytes")
      .register();

    dataReaderProtocolStatusSentAckCount = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_sent_ack_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_matched_publication_protocol_status_sent_ack_count")
      .register();

    dataReaderProtocolStatusSentAckBytes = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_sent_ack_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_matched_publication_protocol_status_sent_ack_bytes")
      .register();

    dataReaderProtocolStatusSentNackCount = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_sent_nack_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_matched_publication_protocol_status_sent_nack_count")
      .register();

    dataReaderProtocolStatusSentNackBytes = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_sent_nack_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_matched_publication_protocol_status_sent_nack_bytes")
      .register();

    dataReaderProtocolStatusReceivedGapCount = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_received_gap_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_matched_publication_protocol_status_received_gap_count")
      .register();

    dataReaderProtocolStatusReceivedGapBytes = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_received_gap_bytes")
      .labelNames(getLabelNames())
      .help("dds_data_reader_matched_publication_protocol_status_received_gap_bytes")
      .register();

    dataReaderProtocolStatusRejectedSampleCount = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_rejected_sample_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_matched_publication_protocol_status_rejected_sample_count")
      .register();

    dataReaderProtocolStatusFirstAvailableSampleSequenceNumberHigh = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_first_available_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "dds_data_reader_matched_publication_protocol_status_first_available_sample_sequence_number_high")
      .register();

    dataReaderProtocolStatusFirstAvailableSampleSequenceNumberLow = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_first_available_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "dds_data_reader_matched_publication_protocol_status_first_available_sample_sequence_number_low")
      .register();

    dataReaderProtocolStatusLastAvailableSampleSequenceNumberHigh = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_last_available_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "dds_data_reader_matched_publication_protocol_status_last_available_sample_sequence_number_high")
      .register();

    dataReaderProtocolStatusLastAvailableSampleSequenceNumberLow = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_last_available_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "dds_data_reader_matched_publication_protocol_status_last_available_sample_sequence_number_low")
      .register();

    dataReaderProtocolStatusLastCommittedSampleSequenceNumberHigh = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_last_committed_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "dds_data_reader_matched_publication_protocol_status_last_committed_sample_sequence_number_high")
      .register();

    dataReaderProtocolStatusLastCommittedSampleSequenceNumberLow = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_last_committed_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "dds_data_reader_matched_publication_protocol_status_last_committed_sample_sequence_number_low")
      .register();

    dataReaderProtocolStatusUncommittedSampleCount = Gauge.build()
      .name(
        "dds_data_reader_matched_publication_protocol_status_uncommitted_sample_count")
      .labelNames(getLabelNames())
      .help("dds_data_reader_matched_publication_protocol_status_uncommitted_sample_count")
      .register();
  }

  public void process(
    DataReaderEntityMatchedPublicationStatistics sample,
    SampleInfo info
  ) {
    // variables
    DataReaderDescription dataReaderDescription;
    SubscriberDescription subscriberDescription = null;
    DomainParticipantDescription domainParticipantDescription = null;

    // try to get descriptions
    dataReaderDescription = descriptionProcessorCache
      .getDataReaderDescription(sample.datareader_key);

    if (dataReaderDescription != null) {
      subscriberDescription = descriptionProcessorCache
        .getSubscriberDescription(dataReaderDescription.subscriber_entity_key);
    }
    if ((dataReaderDescription != null)
      && (subscriberDescription != null)) {
      domainParticipantDescription = descriptionProcessorCache
        .getDomainParticipantDescription(subscriberDescription.participant_entity_key);
    }

    if ((info.instance_state == InstanceStateKind.ALIVE_INSTANCE_STATE)
      && (info.valid_data)
      && (dataReaderDescription != null)
      && (subscriberDescription != null)
      && (domainParticipantDescription != null)) {
      // add / update values
      addUpdateGaugesForLabel(
        info.instance_handle,
        sample,
        domainParticipantDescription,
        subscriberDescription,
        dataReaderDescription
      );
    } else {
      // remove values
      removeLabelsFromGauges(info.instance_handle);
    }
  }

  private void addUpdateGaugesForLabel(
    InstanceHandle_t instanceHandle,
    DataReaderEntityMatchedPublicationStatistics sample,
    DomainParticipantDescription domainParticipantDescription,
    SubscriberDescription subscriberDescription,
    DataReaderDescription dataReaderDescription
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(
      instanceHandle,
      getLabelValues(
        domainParticipantDescription,
        subscriberDescription,
        dataReaderDescription,
        sample
      )
    );

    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(instanceHandle);

    // update gauges
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
      "publication_handle",
    };
  }

  private String[] getLabelValues(
    DomainParticipantDescription domainParticipantDescription,
    SubscriberDescription subscriberDescription,
    DataReaderDescription dataReaderDescription,
    DataReaderEntityMatchedPublicationStatistics matchedPublicationStatistics
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
      BuiltinTopicHelper.toString(matchedPublicationStatistics.publication_handle.value)
    };
  }
}
