package com.github.aguther.dds.examples.prometheus.routing;

import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.RTI.RoutingService.Monitoring.AutoRouteData;
import io.prometheus.client.Gauge;
import java.util.HashMap;

public class AutoRouteDataMetricProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge enabledRouteCount;
  private final Gauge paused;

  public AutoRouteDataMetricProcessor() {
    instanceHandleHashMap = new HashMap<>();

    enabledRouteCount = Gauge.build()
        .name("auto_route_status_set_enabled_route_count")
        .labelNames(getLabelNames())
        .help("auto_route_status_set_enabled_route_count")
        .register();

    paused = Gauge.build()
        .name("auto_route_status_set_paused")
        .labelNames(getLabelNames())
        .help("auto_route_status_set_paused")
        .register();
  }

  public void process(
      AutoRouteData sample,
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
      paused.remove(labelValues);
      // remove instance from hash map
      instanceHandleHashMap.remove(info.instance_handle);
      return;
    }

    // update gauges
    enabledRouteCount.labels(labelValues)
        .set(sample.enabled_route_count);
    paused.labels(labelValues)
        .set(sample.paused ? 1 : 0);
  }

  private String[] getLabelNames() {
    return new String[]{
        "routing_service_name",
        "domain_route_name",
        "session_name",
        "auto_route_name",
    };
  }

  private String[] getLabelValues(
      AutoRouteData sample
  ) {
    return new String[]{
        sample.routing_service_name,
        sample.domain_route_name,
        sample.session_name,
        sample.name,
    };
  }
}
