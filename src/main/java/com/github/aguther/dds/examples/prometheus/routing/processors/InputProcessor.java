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
import idl.RTI.RoutingService.Monitoring.InputConfig;
import idl.RTI.RoutingService.Monitoring.InputEvent;
import idl.RTI.RoutingService.Monitoring.InputPeriodic;
import io.prometheus.client.Gauge;
import java.util.HashMap;
import java.util.regex.Matcher;

class InputProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge ddsRoutingServiceInputState;
  private final Gauge ddsRoutingServiceInputSamplesPerSecond;
  private final Gauge ddsRoutingServiceInputBytesPerSecond;

  InputProcessor() {
    instanceHandleHashMap = new HashMap<>();

    ddsRoutingServiceInputState = Gauge.build()
      .name("dds_routing_service_input_state")
      .labelNames(getLabelNames())
      .help("State of the resource entity expressed as an enumeration of type EntityStateKind "
        + "(0 = INVALID, 1 = ENABLED, 2 = DISABLED, 3 = STARTED, 4 = STOPPED, 5 = RUNNING, 6 = PAUSED)")
      .register();
    ddsRoutingServiceInputSamplesPerSecond = Gauge.build()
      .name("dds_routing_service_input_samples_per_second")
      .labelNames(getLabelNames())
      .help("Statistic variable that provides information about the number of samples processed (received) per second.")
      .register();
    ddsRoutingServiceInputBytesPerSecond = Gauge.build()
      .name("dds_routing_service_input_bytes_per_second")
      .labelNames(getLabelNames())
      .help("Statistic variable that provides information about the number of bytes processed (received) per second.")
      .register();
  }

  void processAddUpdate(
    InstanceHandle_t instanceHandle,
    InputConfig config,
    InputEvent event,
    InputPeriodic periodic
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(instanceHandle, getLabelValues(config));

    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(instanceHandle);

    // update gauges
    ddsRoutingServiceInputState.labels(labelValues)
      .set(event.state.value());
    ddsRoutingServiceInputSamplesPerSecond.labels(labelValues)
      .set(periodic.samples_per_sec.publication_period_metrics.mean);
    ddsRoutingServiceInputBytesPerSecond.labels(labelValues)
      .set(periodic.bytes_per_sec.publication_period_metrics.mean);
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
    ddsRoutingServiceInputState.remove(labelValues);
    ddsRoutingServiceInputSamplesPerSecond.remove(labelValues);
    ddsRoutingServiceInputBytesPerSecond.remove(labelValues);
  }

  private String[] getLabelNames() {
    return new String[]{
      "routing_service",
      "domain_route",
      "session",
      "route",
      "input",
    };
  }

  private String[] getLabelValues(
    InputConfig config
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
      matcher.group(5),
    };
  }
}
