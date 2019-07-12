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

  public DataWriterMatchedSubscriptionWithLocatorMetricsProcessor(
    DescriptionProcessorCache descriptionProcessorCache
  ) {
    checkNotNull(descriptionProcessorCache);
    this.descriptionProcessorCache = descriptionProcessorCache;
    instanceHandleHashMap = new HashMap<>();

    datawriterProtocolStatusPushedSampleCount = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_pushed_sample_count")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_pushed_sample_count")
      .register();

    datawriterProtocolStatusPushedSampleBytes = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_pushed_sample_bytes")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_pushed_sample_bytes")
      .register();

    datawriterProtocolStatusFilteredSampleCount = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_filtered_sample_count")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_filtered_sample_count")
      .register();

    datawriterProtocolStatusFilteredSampleBytes = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_filtered_sample_bytes")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_filtered_sample_bytes")
      .register();

    datawriterProtocolStatusSentHeartbeatCount = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_sent_heartbeat_count")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_sent_heartbeat_count")
      .register();

    datawriterProtocolStatusSentHeartbeatBytes = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_sent_heartbeat_bytes")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_sent_heartbeat_bytes")
      .register();

    datawriterProtocolStatusPulledSampleCount = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_pulled_sample_count")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_pulled_sample_count")
      .register();

    datawriterProtocolStatusPulledSampleBytes = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_pulled_sample_bytes")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_pulled_sample_bytes")
      .register();

    datawriterProtocolStatusReceivedAckCount = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_received_ack_count")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_received_ack_count")
      .register();

    datawriterProtocolStatusReceivedAckBytes = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_received_ack_bytes")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_received_ack_bytes")
      .register();

    datawriterProtocolStatusReceivedNackCount = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_received_nack_count")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_received_nack_count")
      .register();

    datawriterProtocolStatusReceivedNackBytes = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_received_nack_bytes")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_received_nack_bytes")
      .register();

    datawriterProtocolStatusSentGapCount = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_sent_gap_count")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_sent_gap_count")
      .register();

    datawriterProtocolStatusSentGapBytes = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_sent_gap_bytes")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_sent_gap_bytes")
      .register();

    datawriterProtocolStatusRejectedSampleCount = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_rejected_sample_count")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_rejected_sample_count")
      .register();

    datawriterProtocolStatusSendWindowSize = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_send_window_size")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_send_window_size")
      .register();

    datawriterProtocolStatusFirstAvailableSequenceNumberHigh = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_first_available_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_first_available_sequence_number_high")
      .register();

    datawriterProtocolStatusFirstAvailableSequenceNumberLow = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_first_available_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_first_available_sequence_number_low")
      .register();

    datawriterProtocolStatusLastAvailableSequenceNumberHigh = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_last_available_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_last_available_sequence_number_high")
      .register();

    datawriterProtocolStatusLastAvailableSequenceNumberLow = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_last_available_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_last_available_sequence_number_low")
      .register();

    datawriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberHigh = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_first_unacknowledged_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_first_unacknowledged_sample_sequence_number_high")
      .register();

    datawriterProtocolStatusFirstUnacknowledgedSampleSequenceNumberLow = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_first_unacknowledged_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_first_unacknowledged_sample_sequence_number_low")
      .register();

    datawriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberHigh = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_first_available_sample_virtual_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_first_available_sample_virtual_sequence_number_high")
      .register();

    datawriterProtocolStatusFirstAvailableSampleVirtualSequenceNumberLow = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_first_available_sample_virtual_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_first_available_sample_virtual_sequence_number_low")
      .register();

    datawriterProtocolStatusLastAvailableSampleVirtualSequenceNumberHigh = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_last_available_sample_virtual_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_last_available_sample_virtual_sequence_number_high")
      .register();

    datawriterProtocolStatusLastAvailableSampleVirtualSequenceNumberLow = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_last_available_sample_virtual_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_last_available_sample_virtual_sequence_number_low")
      .register();

    datawriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberHigh = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_first_unacknowledged_sample_virtual_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_first_unacknowledged_sample_virtual_sequence_number_high")
      .register();

    datawriterProtocolStatusFirstUnacknowledgedSampleVirtualSequenceNumberLow = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_first_unacknowledged_sample_virtual_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_first_unacknowledged_sample_virtual_sequence_number_low")
      .register();

    datawriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberHigh = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_high")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_high")
      .register();

    datawriterProtocolStatusFirstUnelapsedKeepDurationSampleSequenceNumberLow = Gauge.build()
      .name(
        "datawriter_matched_subscription_locator_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_low")
      .labelNames(getLabelNames())
      .help(
        "datawriter_matched_subscription_locator_protocol_status_first_unelapsed_keep_duration_sample_sequence_number_low")
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
