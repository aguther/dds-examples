package com.github.aguther.dds.examples.prometheus.routing;

import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.RTI.RoutingService.Monitoring.SessionData;
import io.prometheus.client.Gauge;
import java.util.HashMap;

public class SessionDataMetricProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge enabledRouteCount;

  public SessionDataMetricProcessor() {
    instanceHandleHashMap = new HashMap<>();

    enabledRouteCount = Gauge.build()
        .name("session_data_enabled_route_count")
        .labelNames(getLabelNames())
        .help("session_data_enabled_route_count")
        .register();
  }

  public void process(
      SessionData sample,
      SampleInfo info
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(info.instance_handle, getLabelValues(sample));
    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(info.instance_handle);

    // check if sample is alive and contains valid data
    if (info.instance_state != InstanceStateKind.ALIVE_INSTANCE_STATE || !info.valid_data) {
      // remove labels
      enabledRouteCount.remove(labelValues);
      // remove instance from hash map
      instanceHandleHashMap.remove(info.instance_handle);
      return;
    }

    // update gauges
    enabledRouteCount.labels(labelValues).set(sample.enabled_route_count);
  }

  private String[] getLabelNames() {
    return new String[]{
        "routing_service_name",
        "domain_route_name",
        "session_name",
    };
  }

  private String[] getLabelValues(
      SessionData sample
  ) {
    return new String[]{
        sample.routing_service_name,
        sample.domain_route_name,
        sample.name,
    };
  }
}
