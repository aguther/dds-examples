package com.github.aguther.dds.examples.monitoring.prometheus;

import com.github.aguther.dds.util.BuiltinTopicHelper;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.rti.dds.monitoring.TopicEntityStatistics;
import io.prometheus.client.Gauge;
import java.util.HashMap;

class TopicEntityStatisticsMetricProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge inconsistentTopicStatusTotalCount;

  TopicEntityStatisticsMetricProcessor() {
    instanceHandleHashMap = new HashMap<>();

    inconsistentTopicStatusTotalCount = Gauge.build()
        .name("topic_entity_statistics_inconsistent_topic_status_total_count")
        .labelNames(getLabelNames())
        .help("topic_entity_statistics_inconsistent_topic_status_total_count")
        .register();
  }

  void process(
      TopicEntityStatistics sample,
      SampleInfo info
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(info.instance_handle, getLabelValues(sample));
    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(info.instance_handle);

    // check if sample is alive and contains valid data
    if (info.instance_state != InstanceStateKind.ALIVE_INSTANCE_STATE || !info.valid_data) {
      // remove labels
      inconsistentTopicStatusTotalCount.remove(labelValues);
      // remove instance from hash map
      instanceHandleHashMap.remove(info.instance_handle);
      return;
    }

    // update gauges
    inconsistentTopicStatusTotalCount.labels(labelValues).set(
        sample.inconsistent_topic_status.status.total_count);
  }

  private String[] getLabelNames() {
    return new String[]{
        "topic_key",
        "period",
        "participant_key",
        "topic_name",
        "type_name",
        "domain_id",
        "host_id",
        "process_id"
    };
  }

  private String[] getLabelValues(
      TopicEntityStatistics sample
  ) {
    return new String[]{
        BuiltinTopicHelper.toString(sample.topic_key.value),
        Long.toUnsignedString((long) sample.period.sec * 1000000000 + (long) sample.period.nanosec),
        BuiltinTopicHelper.toString(sample.participant_key.value),
        sample.topic_name,
        sample.type_name,
        Integer.toUnsignedString(sample.domain_id),
        Integer.toUnsignedString(sample.host_id),
        Integer.toUnsignedString(sample.process_id),
    };
  }
}
