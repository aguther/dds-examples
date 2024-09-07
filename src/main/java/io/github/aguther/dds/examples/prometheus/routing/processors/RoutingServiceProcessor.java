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
import idl.RTI.RoutingService.Monitoring.ServiceConfig;
import idl.RTI.RoutingService.Monitoring.ServiceEvent;
import idl.RTI.RoutingService.Monitoring.ServicePeriodic;
import io.github.aguther.dds.examples.prometheus.routing.util.ResourceIdMatcher;
import io.prometheus.client.Gauge;
import java.util.HashMap;
import java.util.regex.Matcher;

class RoutingServiceProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge ddsRoutingServiceState;
  private final Gauge ddsRoutingServiceHostTotalMemoryKb;
  private final Gauge ddsRoutingServiceHostTotalSwapMemoryKb;
  private final Gauge ddsRoutingServiceHostUptime;
  private final Gauge ddsRoutingServiceHostCpuUsagePercentage;
  private final Gauge ddsRoutingServiceHostFreeMemoryKb;
  private final Gauge ddsRoutingServiceHostFreeSwapMemoryKb;
  private final Gauge ddsRoutingServiceProcessUptime;
  private final Gauge ddsRoutingServiceProcessCpuUsagePercentage;
  private final Gauge ddsRoutingServiceProcessPhysicalMemoryKb;
  private final Gauge ddsRoutingServiceProcessTotalMemoryKb;

  RoutingServiceProcessor() {
    instanceHandleHashMap = new HashMap<>();

    ddsRoutingServiceState = Gauge.build()
      .name("dds_routing_service_state")
      .labelNames(getLabelNames())
      .help("State of the resource entity expressed as an enumeration of type EntityStateKind "
        + "(0 = INVALID, 1 = ENABLED, 2 = DISABLED, 3 = STARTED, 4 = STOPPED, 5 = RUNNING, 6 = PAUSED)")
      .register();
    ddsRoutingServiceHostTotalMemoryKb = Gauge.build()
      .name("dds_routing_service_host_total_memory_kilobytes")
      .labelNames(getLabelNames())
      .help("Total memory in KiloBytes of the host where the service is running.")
      .register();
    ddsRoutingServiceHostTotalSwapMemoryKb = Gauge.build()
      .name("dds_routing_service_host_total_swap_memory_kilobytes")
      .labelNames(getLabelNames())
      .help("Total swap memory in KiloBytes of the host where the service is running.")
      .register();
    ddsRoutingServiceHostCpuUsagePercentage = Gauge.build()
      .name("dds_routing_service_host_cpu_usage_percentage")
      .labelNames(getLabelNames())
      .help(
        "Statistic variable that provides the global percentage of CPU usage on the host where the service is running.")
      .register();
    ddsRoutingServiceHostUptime = Gauge.build()
      .name("dds_routing_service_host_uptime_seconds")
      .labelNames(getLabelNames())
      .help("Time in seconds elapsed since the host on which the running service started.")
      .register();
    ddsRoutingServiceHostFreeMemoryKb = Gauge.build()
      .name("dds_routing_service_host_free_memory_kilobytes")
      .labelNames(getLabelNames())
      .help(
        "Statistic variable that provides the amount of free memory in KiloBytes of the host where the service is running.")
      .register();
    ddsRoutingServiceHostFreeSwapMemoryKb = Gauge.build()
      .name("dds_routing_service_host_free_swap_memory_kilobytes")
      .labelNames(getLabelNames())
      .help(
        "Statistic variable that provides the amount of free swap memory in KiloBytes of the host where the service is running.")
      .register();
    ddsRoutingServiceProcessUptime = Gauge.build()
      .name("dds_routing_service_process_uptime_seconds")
      .labelNames(getLabelNames())
      .help("Time in seconds elapsed since the running service process started.")
      .register();
    ddsRoutingServiceProcessCpuUsagePercentage = Gauge.build()
      .name("dds_routing_service_process_cpu_usage_percentage")
      .labelNames(getLabelNames())
      .help("Statistic variable that provides the percentage of CPU usage of the process where the service is running.")
      .register();
    ddsRoutingServiceProcessPhysicalMemoryKb = Gauge.build()
      .name("dds_routing_service_process_physical_memory_kilobytes")
      .labelNames(getLabelNames())
      .help(
        "Statistic variable that provides the physical memory utilization in KiloBytes of the process where the service is running.")
      .register();
    ddsRoutingServiceProcessTotalMemoryKb = Gauge.build()
      .name("dds_routing_service_process_total_memory_kilobytes")
      .labelNames(getLabelNames())
      .help(
        "Statistic variable that provides the virtual memory utilization in KiloBytes of the process where the service is running.")
      .register();
  }

  void processAddUpdate(
    InstanceHandle_t instanceHandle,
    ServiceConfig config,
    ServiceEvent event,
    ServicePeriodic periodic
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(instanceHandle, getLabelValues(config));

    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(instanceHandle);

    // update gauges
    ddsRoutingServiceState.labels(labelValues)
      .set(event.state.value());
    ddsRoutingServiceHostTotalMemoryKb.labels(labelValues)
      .set(config.host.total_memory_kb);
    ddsRoutingServiceHostTotalSwapMemoryKb.labels(labelValues)
      .set(config.host.total_swap_memory_kb);
    ddsRoutingServiceHostUptime.labels(labelValues)
      .set(periodic.host.uptime_sec);
    ddsRoutingServiceHostCpuUsagePercentage.labels(labelValues)
      .set(periodic.host.cpu_usage_percentage.publication_period_metrics.mean);
    ddsRoutingServiceHostFreeMemoryKb.labels(labelValues)
      .set(periodic.host.free_memory_kb.publication_period_metrics.mean);
    ddsRoutingServiceHostFreeSwapMemoryKb.labels(labelValues)
      .set(periodic.host.free_swap_memory_kb.publication_period_metrics.mean);
    ddsRoutingServiceProcessUptime.labels(labelValues)
      .set(periodic.process.uptime_sec);
    ddsRoutingServiceProcessCpuUsagePercentage.labels(labelValues)
      .set(periodic.process.cpu_usage_percentage.publication_period_metrics.mean);
    ddsRoutingServiceProcessPhysicalMemoryKb.labels(labelValues)
      .set(periodic.process.physical_memory_kb.publication_period_metrics.mean);
    ddsRoutingServiceProcessTotalMemoryKb.labels(labelValues)
      .set(periodic.process.total_memory_kb.publication_period_metrics.mean);
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
    ddsRoutingServiceState.remove(labelValues);
    ddsRoutingServiceHostTotalMemoryKb.remove(labelValues);
    ddsRoutingServiceHostTotalSwapMemoryKb.remove(labelValues);
    ddsRoutingServiceHostCpuUsagePercentage.remove(labelValues);
    ddsRoutingServiceHostFreeMemoryKb.remove(labelValues);
    ddsRoutingServiceHostFreeSwapMemoryKb.remove(labelValues);
    ddsRoutingServiceProcessCpuUsagePercentage.remove(labelValues);
    ddsRoutingServiceProcessPhysicalMemoryKb.remove(labelValues);
    ddsRoutingServiceProcessTotalMemoryKb.remove(labelValues);
  }

  private String[] getLabelNames() {
    return new String[]{
      "routing_service"
    };
  }

  private String[] getLabelValues(
    ServiceConfig config
  ) {
    Matcher matcher = ResourceIdMatcher.get(config.resource_id);
    if (!matcher.matches()) {
      return new String[]{};
    }
    return new String[]{
      matcher.group(1),
    };
  }
}
