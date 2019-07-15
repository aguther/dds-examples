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
import idl.RTI.RoutingService.Monitoring.DomainRouteConfig;
import idl.RTI.RoutingService.Monitoring.DomainRouteEvent;
import idl.RTI.RoutingService.Monitoring.DomainRoutePeriodic;
import io.prometheus.client.Gauge;
import java.util.HashMap;
import java.util.regex.Matcher;

class DomainRouteProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge ddsRoutingServiceDomainRouteState;
  private final Gauge ddsRoutingServiceDomainRouteInSamplesPerSecond;
  private final Gauge ddsRoutingServiceDomainRouteInBytesPerSecond;
  private final Gauge ddsRoutingServiceDomainRouteOutSamplesPerSecond;
  private final Gauge ddsRoutingServiceDomainRouteOutBytesPerSecond;
  private final Gauge ddsRoutingServiceDomainRouteLatencyMilliseconds;

  DomainRouteProcessor() {
    instanceHandleHashMap = new HashMap<>();

    ddsRoutingServiceDomainRouteState = Gauge.build()
      .name("dds_routing_service_domain_route_state")
      .labelNames(getLabelNames())
      .help("dds_routing_service_domain_route_state")
      .register();
    ddsRoutingServiceDomainRouteInSamplesPerSecond = Gauge.build()
      .name("dds_routing_service_domain_route_in_samples_per_second")
      .labelNames(getLabelNames())
      .help("dds_routing_service_domain_route_in_samples_per_second")
      .register();
    ddsRoutingServiceDomainRouteInBytesPerSecond = Gauge.build()
      .name("dds_routing_service_domain_route_in_bytes_per_second")
      .labelNames(getLabelNames())
      .help("dds_routing_service_domain_route_in_bytes_per_second")
      .register();
    ddsRoutingServiceDomainRouteOutSamplesPerSecond = Gauge.build()
      .name("dds_routing_service_domain_route_out_samples_per_second")
      .labelNames(getLabelNames())
      .help("dds_routing_service_domain_route_out_samples_per_second")
      .register();
    ddsRoutingServiceDomainRouteOutBytesPerSecond = Gauge.build()
      .name("dds_routing_service_domain_route_out_bytes_per_second")
      .labelNames(getLabelNames())
      .help("dds_routing_service_domain_route_out_bytes_per_second")
      .register();
    ddsRoutingServiceDomainRouteLatencyMilliseconds = Gauge.build()
      .name("dds_routing_service_domain_route_latency_milliseconds")
      .labelNames(getLabelNames())
      .help("dds_routing_service_domain_route_latency_milliseconds")
      .register();
  }

  void processAddUpdate(
    InstanceHandle_t instanceHandle,
    DomainRouteConfig config,
    DomainRouteEvent event,
    DomainRoutePeriodic periodic
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(instanceHandle, getLabelValues(config));

    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(instanceHandle);

    // update gauges
    ddsRoutingServiceDomainRouteState.labels(labelValues)
      .set(event.state.value());
    ddsRoutingServiceDomainRouteInSamplesPerSecond.labels(labelValues)
      .set(periodic.in_samples_per_sec.publication_period_metrics.mean);
    ddsRoutingServiceDomainRouteInBytesPerSecond.labels(labelValues)
      .set(periodic.in_bytes_per_sec.publication_period_metrics.mean);
    ddsRoutingServiceDomainRouteOutSamplesPerSecond.labels(labelValues)
      .set(periodic.out_samples_per_sec.publication_period_metrics.mean);
    ddsRoutingServiceDomainRouteOutBytesPerSecond.labels(labelValues)
      .set(periodic.out_bytes_per_sec.publication_period_metrics.mean);
    ddsRoutingServiceDomainRouteLatencyMilliseconds.labels(labelValues)
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
    ddsRoutingServiceDomainRouteState.remove(labelValues);
    ddsRoutingServiceDomainRouteInSamplesPerSecond.remove(labelValues);
    ddsRoutingServiceDomainRouteInBytesPerSecond.remove(labelValues);
    ddsRoutingServiceDomainRouteOutSamplesPerSecond.remove(labelValues);
    ddsRoutingServiceDomainRouteOutBytesPerSecond.remove(labelValues);
    ddsRoutingServiceDomainRouteLatencyMilliseconds.remove(labelValues);
  }

  private String[] getLabelNames() {
    return new String[]{
      "routing_service",
      "domain_route",
    };
  }

  private String[] getLabelValues(
    DomainRouteConfig config
  ) {
    Matcher matcher = ResourceIdMatcher.get(config.resource_id);
    if (!matcher.matches()) {
      return new String[]{};
    }
    return new String[]{
      matcher.group(1),
      matcher.group(2),
    };
  }
}
