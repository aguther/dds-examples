package com.github.aguther.dds.examples.monitoring.prometheus;

import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.RTI.RoutingService.Monitoring.RoutingServiceData;
import io.prometheus.client.Gauge;
import java.util.HashMap;

class RoutingServiceDataMetricProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge dummy;

  RoutingServiceDataMetricProcessor() {
    instanceHandleHashMap = new HashMap<>();

    dummy = Gauge.build()
        .name("routing_service_data")
        .labelNames(getLabelNames())
        .help("routing_service_data")
        .register();
  }

  void process(
      RoutingServiceData sample,
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
    dummy.labels(labelValues).set(1);
  }

  private String[] getLabelNames() {
    return new String[]{
        "name",
        "group_name",
        "host_name",
        "host_id",
        "app_id",
    };
  }

  private String[] getLabelValues(
      RoutingServiceData sample
  ) {
    return new String[]{
        sample.name,
        sample.group_name,
        sample.host_name,
        Integer.toUnsignedString(sample.host_id),
        Integer.toUnsignedString(sample.app_id),
    };
  }
}
