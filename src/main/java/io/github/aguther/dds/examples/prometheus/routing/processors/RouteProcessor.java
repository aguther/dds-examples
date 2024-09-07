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

package io.github.aguther.dds.examples.prometheus.routing.processors;

import com.rti.dds.infrastructure.InstanceHandle_t;
import idl.RTI.RoutingService.Monitoring.RouteConfig;
import idl.RTI.RoutingService.Monitoring.RouteEvent;
import idl.RTI.RoutingService.Monitoring.RoutePeriodic;
import io.github.aguther.dds.examples.prometheus.routing.util.ResourceIdMatcher;
import io.prometheus.client.Gauge;
import java.util.HashMap;
import java.util.regex.Matcher;

class RouteProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge ddsRoutingServiceRouteState;
  private final Gauge ddsRoutingServiceRouteInSamplesPerSecond;
  private final Gauge ddsRoutingServiceRouteInBytesPerSecond;
  private final Gauge ddsRoutingServiceRouteOutSamplesPerSecond;
  private final Gauge ddsRoutingServiceRouteOutBytesPerSecond;
  private final Gauge ddsRoutingServiceRouteLatencyMilliseconds;

  RouteProcessor() {
    instanceHandleHashMap = new HashMap<>();

    ddsRoutingServiceRouteState = Gauge.build()
      .name("dds_routing_service_route_state")
      .labelNames(getLabelNames())
      .help("State of the resource entity expressed as an enumeration of type EntityStateKind "
        + "(0 = INVALID, 1 = ENABLED, 2 = DISABLED, 3 = STARTED, 4 = STOPPED, 5 = RUNNING, 6 = PAUSED)")
      .register();
    ddsRoutingServiceRouteInSamplesPerSecond = Gauge.build()
      .name("dds_routing_service_route_in_samples_per_second")
      .labelNames(getLabelNames())
      .help("Statistic variable that provides information about the number of samples processed (received) per second.")
      .register();
    ddsRoutingServiceRouteInBytesPerSecond = Gauge.build()
      .name("dds_routing_service_route_in_bytes_per_second")
      .labelNames(getLabelNames())
      .help("Statistic variable that provides information about the number of bytes processed (received) per second.")
      .register();
    ddsRoutingServiceRouteOutSamplesPerSecond = Gauge.build()
      .name("dds_routing_service_route_out_samples_per_second")
      .labelNames(getLabelNames())
      .help("Statistic variable that provides information about the number of samples processed (sent) per second.")
      .register();
    ddsRoutingServiceRouteOutBytesPerSecond = Gauge.build()
      .name("dds_routing_service_route_out_bytes_per_second")
      .labelNames(getLabelNames())
      .help("Statistic variable that provides information about the number of bytes processed (sent) per second.")
      .register();
    ddsRoutingServiceRouteLatencyMilliseconds = Gauge.build()
      .name("dds_routing_service_route_latency_milliseconds")
      .labelNames(getLabelNames())
      .help("Statistic variable that provides information about the latency in milliseconds for the data processed. "
        + "The latency in a refers to the total time elapsed during the associated processing of the data, which "
        + "depends on the type of application.")
      .register();
  }

  void processAddUpdate(
    InstanceHandle_t instanceHandle,
    RouteConfig config,
    RouteEvent event,
    RoutePeriodic periodic
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(instanceHandle, getLabelValues(config));

    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(instanceHandle);

    // update gauges
    ddsRoutingServiceRouteState.labels(labelValues)
      .set(event.state.value());
    ddsRoutingServiceRouteInSamplesPerSecond.labels(labelValues)
      .set(periodic.in_samples_per_sec.publication_period_metrics.mean);
    ddsRoutingServiceRouteInBytesPerSecond.labels(labelValues)
      .set(periodic.in_bytes_per_sec.publication_period_metrics.mean);
    ddsRoutingServiceRouteOutSamplesPerSecond.labels(labelValues)
      .set(periodic.out_samples_per_sec.publication_period_metrics.mean);
    ddsRoutingServiceRouteOutBytesPerSecond.labels(labelValues)
      .set(periodic.out_bytes_per_sec.publication_period_metrics.mean);
    ddsRoutingServiceRouteLatencyMilliseconds.labels(labelValues)
      .set(periodic.latency_millisec.publication_period_metrics.mean);
  }

  void processRemove(
    InstanceHandle_t instanceHandle
  ) {
    // check if remove is necessary
    if (!instanceHandleHashMap.containsKey(instanceHandle)) {
      return;
    }

    // get and remove label values
    final String[] labelValues = instanceHandleHashMap.remove(instanceHandle);

    // remove labels
    ddsRoutingServiceRouteState.remove(labelValues);
    ddsRoutingServiceRouteInSamplesPerSecond.remove(labelValues);
    ddsRoutingServiceRouteInBytesPerSecond.remove(labelValues);
    ddsRoutingServiceRouteOutSamplesPerSecond.remove(labelValues);
    ddsRoutingServiceRouteOutBytesPerSecond.remove(labelValues);
    ddsRoutingServiceRouteLatencyMilliseconds.remove(labelValues);
  }

  private String[] getLabelNames() {
    return new String[]{
      "routing_service",
      "domain_route",
      "session",
      "route",
    };
  }

  private String[] getLabelValues(
    RouteConfig config
  ) {
    Matcher matcher = ResourceIdMatcher.get(config.resource_id);
    if (!matcher.matches()) {
      return new String[]{};
    }
    return new String[]{
      matcher.group(1),
      matcher.group(2),
      matcher.group(3),
      matcher.group(4),
    };
  }
}
