package com.github.aguther.dds.examples.monitoring.prometheus;

import com.github.aguther.dds.util.BuiltinTopicHelper;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.rti.dds.monitoring.DataReaderDescription;
import io.prometheus.client.Gauge;
import java.util.HashMap;

class DataReaderDescriptionMetricProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge serializedSampleMaxSize;
  private final Gauge serializedSampleMinSize;
  private final Gauge serializedKeyMaxSize;

  DataReaderDescriptionMetricProcessor() {
    instanceHandleHashMap = new HashMap<>();

    serializedSampleMaxSize = Gauge.build()
        .name("datareader_description_serialized_sample_max_size")
        .labelNames(getLabelNames())
        .help("datareader_description_serialized_sample_max_size")
        .register();

    serializedSampleMinSize = Gauge.build()
        .name("datareader_description_serialized_sample_min_size")
        .labelNames(getLabelNames())
        .help("datareader_description_serialized_sample_min_size")
        .register();

    serializedKeyMaxSize = Gauge.build()
        .name("datareader_description_serialized_key_max_size")
        .labelNames(getLabelNames())
        .help("datareader_description_serialized_key_max_size")
        .register();
  }

  void process(
      DataReaderDescription sample,
      SampleInfo info
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(info.instance_handle, getLabelValues(sample));
    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(info.instance_handle);

    // check if sample is alive and contains valid data
    if (info.instance_state != InstanceStateKind.ALIVE_INSTANCE_STATE || !info.valid_data) {
      // remove labels
      serializedSampleMaxSize.remove(labelValues);
      serializedSampleMinSize.remove(labelValues);
      serializedKeyMaxSize.remove(labelValues);
      // remove instance from hash map
      instanceHandleHashMap.remove(info.instance_handle);
      return;
    }

    // update gauges
    serializedSampleMaxSize.labels(labelValues).set(sample.serialized_sample_max_size);
    serializedSampleMinSize.labels(labelValues).set(sample.serialized_sample_min_size);
    serializedKeyMaxSize.labels(labelValues).set(sample.serialized_key_max_size);
  }

  private String[] getLabelNames() {
    return new String[]{
        "entity_key",
        "subscriber_entity_key",
        "topic_entity_key",
        "domain_id",
        "host_id",
        "process_id",
        "type_name",
        "topic_name",
        "subscription_name",
        "subscription_role_name"
    };
  }

  private String[] getLabelValues(
      DataReaderDescription sample
  ) {
    return new String[]{
        BuiltinTopicHelper.toString(sample.entity_key.value),
        BuiltinTopicHelper.toString(sample.subscriber_entity_key.value),
        BuiltinTopicHelper.toString(sample.topic_entity_key.value),
        Integer.toUnsignedString(sample.domain_id),
        Integer.toUnsignedString(sample.host_id),
        Integer.toUnsignedString(sample.process_id),
        sample.type_name,
        sample.topic_name,
        sample.qos.subscription_name.name,
        sample.qos.subscription_name.role_name
    };
  }
}
