package com.github.aguther.dds.examples.monitoring.prometheus;

import com.github.aguther.dds.util.BuiltinTopicHelper;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.rti.dds.monitoring.SubscriberDescription;
import io.prometheus.client.Gauge;
import java.util.HashMap;

class SubscriberDescriptionMetricProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge dummy;

  SubscriberDescriptionMetricProcessor() {
    instanceHandleHashMap = new HashMap<>();

    dummy = Gauge.build()
        .name("subscriber_description")
        .labelNames(getLabelNames())
        .help("subscriber_description")
        .register();
  }

  void process(
      SubscriberDescription sample,
      SampleInfo info
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(info.instance_handle, getLabelValues(sample));
    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(info.instance_handle);

    // check if sample is alive and contains valid data
    if (info.instance_state != InstanceStateKind.ALIVE_INSTANCE_STATE || !info.valid_data) {
      // remove labels
      dummy.remove(labelValues);
      // remove instance from hash map
      instanceHandleHashMap.remove(info.instance_handle);
      return;
    }

    // update gauges
    dummy.labels(getLabelValues(sample)).set(1);
  }

  private String[] getLabelNames() {
    return new String[]{
        "entity_key",
        "participant_entity_key",
        "domain_id",
        "host_id",
        "process_id",
        "subscriber_name",
        "subscriber_role_name"
    };
  }

  private String[] getLabelValues(
      SubscriberDescription sample
  ) {
    return new String[]{
        BuiltinTopicHelper.toString(sample.entity_key.value),
        BuiltinTopicHelper.toString(sample.participant_entity_key.value),
        Integer.toUnsignedString(sample.domain_id),
        Integer.toUnsignedString(sample.host_id),
        Integer.toUnsignedString(sample.process_id),
        sample.qos.subscriber_name.name,
        sample.qos.subscriber_name.role_name
    };
  }
}
