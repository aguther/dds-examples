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

package com.github.aguther.dds.examples.prometheus.routing.processors;

import com.github.aguther.dds.examples.prometheus.routing.util.ResourceIdMatcher;
import com.rti.dds.infrastructure.InstanceHandle_t;
import idl.RTI.RoutingService.Monitoring.AutoRouteConfig;
import idl.RTI.RoutingService.Monitoring.AutoRouteEvent;
import idl.RTI.RoutingService.Monitoring.AutoRoutePeriodic;
import io.prometheus.client.Gauge;
import java.util.HashMap;
import java.util.regex.Matcher;

class AutoRouteProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge ddsRoutingServiceAutoRouteState;
  private final Gauge ddsRoutingServiceAutoRouteInSamplesPerSecond;
  private final Gauge ddsRoutingServiceAutoRouteInBytesPerSecond;
  private final Gauge ddsRoutingServiceAutoRouteOutSamplesPerSecond;
  private final Gauge ddsRoutingServiceAutoRouteOutBytesPerSecond;
  private final Gauge ddsRoutingServiceAutoRouteLatencyMilliseconds;

  AutoRouteProcessor() {
    instanceHandleHashMap = new HashMap<>();

    ddsRoutingServiceAutoRouteState = Gauge.build()
      .name("dds_routing_service_auto_route_state")
      .labelNames(getLabelNames())
      .help("State of the resource entity expressed as an enumeration of type EntityStateKind "
        + "(0 = INVALID, 1 = ENABLED, 2 = DISABLED, 3 = STARTED, 4 = STOPPED, 5 = RUNNING, 6 = PAUSED)")
      .register();
    ddsRoutingServiceAutoRouteInSamplesPerSecond = Gauge.build()
      .name("dds_routing_service_auto_route_in_samples_per_second")
      .labelNames(getLabelNames())
      .help("Statistic variable that provides information about the number of samples processed (received) per second.")
      .register();
    ddsRoutingServiceAutoRouteInBytesPerSecond = Gauge.build()
      .name("dds_routing_service_auto_route_in_bytes_per_second")
      .labelNames(getLabelNames())
      .help("Statistic variable that provides information about the number of bytes processed (received) per second.")
      .register();
    ddsRoutingServiceAutoRouteOutSamplesPerSecond = Gauge.build()
      .name("dds_routing_service_auto_route_out_samples_per_second")
      .labelNames(getLabelNames())
      .help("Statistic variable that provides information about the number of samples processed (sent) per second.")
      .register();
    ddsRoutingServiceAutoRouteOutBytesPerSecond = Gauge.build()
      .name("dds_routing_service_auto_route_out_bytes_per_second")
      .labelNames(getLabelNames())
      .help("Statistic variable that provides information about the number of bytes processed (sent) per second.")
      .register();
    ddsRoutingServiceAutoRouteLatencyMilliseconds = Gauge.build()
      .name("dds_routing_service_auto_route_latency_milliseconds")
      .labelNames(getLabelNames())
      .help("Statistic variable that provides information about the latency in milliseconds for the data processed. "
        + "The latency in a refers to the total time elapsed during the associated processing of the data, which "
        + "depends on the type of application.")
      .register();
  }

  void processAddUpdate(
    InstanceHandle_t instanceHandle,
    AutoRouteConfig config,
    AutoRouteEvent event,
    AutoRoutePeriodic periodic
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(instanceHandle, getLabelValues(config));

    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(instanceHandle);

    // update gauges
    ddsRoutingServiceAutoRouteState.labels(labelValues)
      .set(event.state.value());
    ddsRoutingServiceAutoRouteInSamplesPerSecond.labels(labelValues)
      .set(periodic.in_samples_per_sec.publication_period_metrics.mean);
    ddsRoutingServiceAutoRouteInBytesPerSecond.labels(labelValues)
      .set(periodic.in_bytes_per_sec.publication_period_metrics.mean);
    ddsRoutingServiceAutoRouteOutSamplesPerSecond.labels(labelValues)
      .set(periodic.out_samples_per_sec.publication_period_metrics.mean);
    ddsRoutingServiceAutoRouteOutBytesPerSecond.labels(labelValues)
      .set(periodic.out_bytes_per_sec.publication_period_metrics.mean);
    ddsRoutingServiceAutoRouteLatencyMilliseconds.labels(labelValues)
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
    ddsRoutingServiceAutoRouteState.remove(labelValues);
    ddsRoutingServiceAutoRouteInSamplesPerSecond.remove(labelValues);
    ddsRoutingServiceAutoRouteInBytesPerSecond.remove(labelValues);
    ddsRoutingServiceAutoRouteOutSamplesPerSecond.remove(labelValues);
    ddsRoutingServiceAutoRouteOutBytesPerSecond.remove(labelValues);
    ddsRoutingServiceAutoRouteLatencyMilliseconds.remove(labelValues);
  }

  private String[] getLabelNames() {
    return new String[]{
      "routing_service",
      "domain_route",
      "session",
      "auto_route",
    };
  }

  private String[] getLabelValues(
    AutoRouteConfig config
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
