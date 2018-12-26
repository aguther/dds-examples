package com.github.aguther.dds.examples.monitoring.prometheus;

import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.RTI.RoutingService.Monitoring.DomainRouteData;
import io.prometheus.client.Gauge;
import java.util.HashMap;

class DomainRouteDataMetricProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge dummy;

  DomainRouteDataMetricProcessor() {
    instanceHandleHashMap = new HashMap<>();

    dummy = Gauge.build()
        .name("domain_route_data")
        .labelNames(getLabelNames())
        .help("domain_route_data")
        .register();
  }

  void process(
      DomainRouteData sample,
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
    dummy.labels(labelValues).set(0);
  }

  private String[] getLabelNames() {
    return new String[]{
        "routing_service_name",
        "name",
    };
  }

  private String[] getLabelValues(
      DomainRouteData sample
  ) {
    return new String[]{
        sample.routing_service_name,
        sample.name,
    };
  }
}
