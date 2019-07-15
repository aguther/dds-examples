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
import com.rti.dds.infrastructure.Locator_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.rti.dds.monitoring.DataWriterDescription;
import idl.rti.dds.monitoring.DataWriterEntityMatchedSubscriptionWithLocatorStatistics;
import idl.rti.dds.monitoring.DomainParticipantDescription;
import idl.rti.dds.monitoring.PublisherDescription;
import io.prometheus.client.Gauge;
import java.util.HashMap;

public class DataWriterMatchedSubscriptionWithLocatorMetricsProcessor {

  private final DescriptionProcessorCache descriptionProcessorCache;
  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

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

  public DataWriterMatchedSubscriptionWithLocatorMetricsProcessor(
    DescriptionProcessorCache descriptionProcessorCache
  ) {
    checkNotNull(descriptionProcessorCache);
    this.descriptionProcessorCache = descriptionProcessorCache;
    instanceHandleHashMap = new HashMap<>();

    dataWriterProtocolStatusPushedSampleCount = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_pushed_sample_count")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_pushed_sample_count")
      .register();

    dataWriterProtocolStatusPushedSampleBytes = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_pushed_sample_bytes")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_pushed_sample_bytes")
      .register();

    dataWriterProtocolStatusFilteredSampleCount = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_filtered_sample_count")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_filtered_sample_count")
      .register();

    dataWriterProtocolStatusFilteredSampleBytes = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_filtered_sample_bytes")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_filtered_sample_bytes")
      .register();

    dataWriterProtocolStatusSentHeartbeatCount = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_sent_heartbeat_count")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_sent_heartbeat_count")
      .register();

    dataWriterProtocolStatusSentHeartbeatBytes = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_sent_heartbeat_bytes")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_sent_heartbeat_bytes")
      .register();

    dataWriterProtocolStatusPulledSampleCount = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_pulled_sample_count")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_pulled_sample_count")
      .register();

    dataWriterProtocolStatusPulledSampleBytes = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_pulled_sample_bytes")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_pulled_sample_bytes")
      .register();

    dataWriterProtocolStatusReceivedAckCount = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_received_ack_count")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_received_ack_count")
      .register();

    dataWriterProtocolStatusReceivedAckBytes = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_received_ack_bytes")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_received_ack_bytes")
      .register();

    dataWriterProtocolStatusReceivedNackCount = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_received_nack_count")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_received_nack_count")
      .register();

    dataWriterProtocolStatusReceivedNackBytes = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_received_nack_bytes")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_received_nack_bytes")
      .register();

    dataWriterProtocolStatusSentGapCount = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_sent_gap_count")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_sent_gap_count")
      .register();

    dataWriterProtocolStatusSentGapBytes = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_sent_gap_bytes")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_sent_gap_bytes")
      .register();

    dataWriterProtocolStatusRejectedSampleCount = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_rejected_sample_count")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_rejected_sample_count")
      .register();

    dataWriterProtocolStatusSendWindowSize = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_send_window_size")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_send_window_size")
      .register();

    dataWriterProtocolStatusFirstAvailableSequenceNumberHigh = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_available_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_available_sequence_number_high")
      .register();

    dataWriterProtocolStatusFirstAvailableSequenceNumberLow = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_available_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_available_sequence_number_low")
      .register();

    dataWriterProtocolStatusLastAvailableSequenceNumberHigh = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_last_available_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_last_available_sequence_number_high")
      .register();

    dataWriterProtocolStatusLastAvailableSequenceNumberLow = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_last_available_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_last_available_sequence_number_low")
      .register();

    dataWriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberHigh = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_unacknowledged_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_unacknowledged_sample_sequence_number_high")
      .register();

    dataWriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberLow = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_unacknowledged_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_unacknowledged_sample_sequence_number_low")
      .register();

    dataWriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberHigh = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_available_sample_virtual_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_available_sample_virtual_sequence_number_high")
      .register();

    dataWriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberLow = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_available_sample_virtual_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_available_sample_virtual_sequence_number_low")
      .register();

    dataWriterProtocolStatusLastAvailableSampleVirtualSequenceNumberHigh = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_last_available_sample_virtual_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_last_available_sample_virtual_sequence_number_high")
      .register();

    dataWriterProtocolStatusLastAvailableSampleVirtualSequenceNumberLow = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_last_available_sample_virtual_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_last_available_sample_virtual_sequence_number_low")
      .register();

    dataWriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberHigh = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_unacknowledged_sample_virtual_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_unacknowledged_sample_virtual_sequence_number_high")
      .register();

    dataWriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberLow = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_unacknowledged_sample_virtual_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_unacknowledged_sample_virtual_sequence_number_low")
      .register();

    dataWriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberHigh = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_high")
      .register();

    dataWriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberLow = Gauge.build()
      .name(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "dds_data_writer_matched_subscription_locator_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_low")
      .register();
  }

  public void process(
    DataWriterEntityMatchedSubscriptionWithLocatorStatistics sample,
    SampleInfo info
  ) {
    // variables
    DataWriterDescription dataWriterDescription;
    PublisherDescription publisherDescription = null;
    DomainParticipantDescription domainParticipantDescription = null;

    // try to get descriptions
    dataWriterDescription = descriptionProcessorCache
      .getDataWriterDescription(sample.datawriter_key);

    if (dataWriterDescription != null) {
      publisherDescription = descriptionProcessorCache
        .getPublisherDescription(dataWriterDescription.publisher_entity_key);
    }
    if ((dataWriterDescription != null)
      && (publisherDescription != null)) {
      domainParticipantDescription = descriptionProcessorCache
        .getDomainParticipantDescription(publisherDescription.participant_entity_key);
    }

    if ((info.instance_state == InstanceStateKind.ALIVE_INSTANCE_STATE)
      && (info.valid_data)
      && (dataWriterDescription != null)
      && (publisherDescription != null)
      && (domainParticipantDescription != null)) {
      // add / update values
      addUpdateGaugesForLabel(
        info.instance_handle,
        sample,
        domainParticipantDescription,
        publisherDescription,
        dataWriterDescription
      );
    } else {
      // remove values
      removeLabelsFromGauges(info.instance_handle);
    }
  }

  private void addUpdateGaugesForLabel(
    InstanceHandle_t instanceHandle,
    DataWriterEntityMatchedSubscriptionWithLocatorStatistics sample,
    DomainParticipantDescription domainParticipantDescription,
    PublisherDescription publisherDescription,
    DataWriterDescription dataWriterDescription
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(
      instanceHandle,
      getLabelValues(
        domainParticipantDescription,
        publisherDescription,
        dataWriterDescription,
        sample
      )
    );

    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(instanceHandle);

    // update gauges
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
      "subscription_locator_kind",
      "subscription_locator_address",
      "subscription_locator_port",
    };
  }

  private String[] getLabelValues(
    DomainParticipantDescription domainParticipantDescription,
    PublisherDescription publisherDescription,
    DataWriterDescription dataWriterDescription,
    DataWriterEntityMatchedSubscriptionWithLocatorStatistics matchedSubscriptionStatistics
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
      getLocatorKindString(matchedSubscriptionStatistics.subscription_locator.kind),
      BuiltinTopicHelper.toString(matchedSubscriptionStatistics.subscription_locator.address),
      Integer.toString(matchedSubscriptionStatistics.subscription_locator._port)
    };
  }

  private String getLocatorKindString(
    int locatorKind
  ) {
    switch (locatorKind) {
      case Locator_t.ADDRESS_LENGTH_MAX:
        return "ADDRESS_LENGTH_MAX";
      case Locator_t.KIND_DTLS:
        return "KIND_DTLS";
      case Locator_t.KIND_INTRA:
        return "KIND_INTRA";
      case Locator_t.KIND_INVALID:
        return "KIND_INVALID";
      case Locator_t.KIND_RESERVED:
        return "KIND_RESERVED";
      case Locator_t.KIND_SHMEM:
        return "KIND_SHMEM";
      case Locator_t.KIND_TCPV4_LAN:
        return "KIND_TCPV4_LAN";
      case Locator_t.KIND_TCPV4_WAN:
        return "KIND_TCPV4_WAN";
      case Locator_t.KIND_TLSV4_LAN:
        return "KIND_TLSV4_LAN";
      case Locator_t.KIND_TLSV4_WAN:
        return "KIND_TLSV4_WAN";
      case Locator_t.KIND_UDPv4:
        return "KIND_UDPv4";
      case Locator_t.KIND_UDPv6:
        return "KIND_UDPv6";
      case Locator_t.KIND_UDPv6_510:
        return "KIND_UDPv6_510";
      case Locator_t.KIND_WAN:
        return "KIND_WAN";
      case Locator_t.PORT_INVALID:
        return "PORT_INVALID";
      default:
        return "UNKNOWN";
    }
  }
}
