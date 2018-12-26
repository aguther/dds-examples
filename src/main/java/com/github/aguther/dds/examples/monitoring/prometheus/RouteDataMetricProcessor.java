package com.github.aguther.dds.examples.monitoring.prometheus;

import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.RTI.RoutingService.Monitoring.RouteData;
import io.prometheus.client.Gauge;
import java.util.HashMap;

class RouteDataMetricProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge paused;

  RouteDataMetricProcessor() {
    instanceHandleHashMap = new HashMap<>();

    paused = Gauge.build()
        .name("route_status_set_paused")
        .labelNames(getLabelNames())
        .help("route_status_set_paused")
        .register();
  }

  void process(
      RouteData sample,
      SampleInfo info
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(info.instance_handle, getLabelValues(sample));
    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(info.instance_handle);

    // check if sample is alive and contains valid data
    if (info.instance_state != InstanceStateKind.ALIVE_INSTANCE_STATE || !info.valid_data) {
      // remove labels
      paused.remove(labelValues);
      // remove instance from hash map
      instanceHandleHashMap.remove(info.instance_handle);
      return;
    }

    // update gauges
    paused.labels(labelValues)
        .set(sample.paused ? 1 : 0);
  }

  private String[] getLabelNames() {
    return new String[]{
        "routing_service_name",
        "domain_route_name",
        "session_name",
        "name",
    };
  }

  private String[] getLabelValues(
      RouteData sample
  ) {
    return new String[]{
        sample.routing_service_name,
        sample.domain_route_name,
        sample.session_name,
        sample.name,
    };
  }
}
