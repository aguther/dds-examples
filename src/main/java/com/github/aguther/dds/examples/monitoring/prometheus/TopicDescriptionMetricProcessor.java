package com.github.aguther.dds.examples.monitoring.prometheus;

import com.github.aguther.dds.util.BuiltinTopicHelper;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.rti.dds.monitoring.TopicDescription;
import io.prometheus.client.Gauge;
import java.util.HashMap;

class TopicDescriptionMetricProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge tcSerializedSize;

  TopicDescriptionMetricProcessor() {
    instanceHandleHashMap = new HashMap<>();

    tcSerializedSize = Gauge.build()
        .name("topic_description_tc_serialized_size")
        .labelNames(getLabelNames())
        .help("topic_description_tc_serialized_size")
        .register();
  }

  void process(
      TopicDescription sample,
      SampleInfo info
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(info.instance_handle, getLabelValues(sample));
    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(info.instance_handle);

    // check if sample is alive and contains valid data
    if (info.instance_state != InstanceStateKind.ALIVE_INSTANCE_STATE || !info.valid_data) {
      // remove labels
      tcSerializedSize.remove(labelValues);
      // remove instance from hash map
      instanceHandleHashMap.remove(info.instance_handle);
      return;
    }

    // update gauges
    tcSerializedSize.labels(getLabelValues(sample)).set(sample.tc_serialized_size);
  }

  private String[] getLabelNames() {
    return new String[]{
        "entity_key",
        "participant_entity_key",
        "domain_id",
        "host_id",
        "process_id",
        "topic_name",
        "type_name",
    };
  }

  private String[] getLabelValues(
      TopicDescription sample
  ) {
    return new String[]{
        BuiltinTopicHelper.toString(sample.entity_key.value),
        BuiltinTopicHelper.toString(sample.participant_entity_key.value),
        Integer.toUnsignedString(sample.domain_id),
        Integer.toUnsignedString(sample.host_id),
        Integer.toUnsignedString(sample.process_id),
        sample.topic_name,
        sample.type_name
    };
  }
}
