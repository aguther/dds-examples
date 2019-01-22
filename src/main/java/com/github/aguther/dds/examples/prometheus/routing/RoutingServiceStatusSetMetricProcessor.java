package com.github.aguther.dds.examples.prometheus.routing;

import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.RTI.RoutingService.Monitoring.RoutingServiceStatusSet;
import io.prometheus.client.Gauge;
import java.util.HashMap;

public class RoutingServiceStatusSetMetricProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge cpuUsagePercentagePeriodMs;
  private final Gauge cpuUsagePercentageCount;
  private final Gauge cpuUsagePercentageMean;
  private final Gauge cpuUsagePercentageMinimum;
  private final Gauge cpuUsagePercentageMaximum;
  private final Gauge cpuUsagePercentageStdDev;
  private final Gauge physicalMemoryKbPeriodMs;
  private final Gauge physicalMemoryKbCount;
  private final Gauge physicalMemoryKbMean;
  private final Gauge physicalMemoryKbMinimum;
  private final Gauge physicalMemoryKbMaximum;
  private final Gauge physicalMemoryKbStdDev;
  private final Gauge totalMemoryKbPeriodMs;
  private final Gauge totalMemoryKbCount;
  private final Gauge totalMemoryKbMean;
  private final Gauge totalMemoryKbMinimum;
  private final Gauge totalMemoryKbMaximum;
  private final Gauge totalMemoryKbStdDev;
  private final Gauge uptime;
  private final Gauge hostCpuUsagePercentagePeriodMs;
  private final Gauge hostCpuUsagePercentageCount;
  private final Gauge hostCpuUsagePercentageMean;
  private final Gauge hostCpuUsagePercentageMinimum;
  private final Gauge hostCpuUsagePercentageMaximum;
  private final Gauge hostCpuUsagePercentageStdDev;
  private final Gauge hostFreeMemoryKbPeriodMs;
  private final Gauge hostFreeMemoryKbCount;
  private final Gauge hostFreeMemoryKbMean;
  private final Gauge hostFreeMemoryKbMinimum;
  private final Gauge hostFreeMemoryKbMaximum;
  private final Gauge hostFreeMemoryKbStdDev;
  private final Gauge hostTotalMemoryKb;
  private final Gauge hostFreeSwapMemoryKbPeriodMs;
  private final Gauge hostFreeSwapMemoryKbCount;
  private final Gauge hostFreeSwapMemoryKbMean;
  private final Gauge hostFreeSwapMemoryKbMinimum;
  private final Gauge hostFreeSwapMemoryKbMaximum;
  private final Gauge hostFreeSwapMemoryKbStdDev;
  private final Gauge hostTotalSwapMemoryKb;
  private final Gauge hostUptime;

  public RoutingServiceStatusSetMetricProcessor() {
    instanceHandleHashMap = new HashMap<>();

    cpuUsagePercentagePeriodMs = Gauge.build()
        .name("routing_service_status_set_cpu_usage_percentage_period_ms")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_cpu_usage_percentage_period_ms")
        .register();

    cpuUsagePercentageCount = Gauge.build()
        .name("routing_service_status_set_cpu_usage_percentage_count")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_cpu_usage_percentage_count")
        .register();

    cpuUsagePercentageMean = Gauge.build()
        .name("routing_service_status_set_cpu_usage_percentage_mean")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_cpu_usage_percentage_mean")
        .register();

    cpuUsagePercentageMinimum = Gauge.build()
        .name("routing_service_status_set_cpu_usage_percentage_minimum")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_cpu_usage_percentage_minimum")
        .register();

    cpuUsagePercentageMaximum = Gauge.build()
        .name("routing_service_status_set_cpu_usage_percentage_maximum")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_cpu_usage_percentage_maximum")
        .register();

    cpuUsagePercentageStdDev = Gauge.build()
        .name("routing_service_status_set_cpu_usage_percentage_std_dev")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_cpu_usage_percentage_std_dev")
        .register();

    physicalMemoryKbPeriodMs = Gauge.build()
        .name("routing_service_status_set_physical_memory_kb_period_ms")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_physical_memory_kb_period_ms")
        .register();

    physicalMemoryKbCount = Gauge.build()
        .name("routing_service_status_set_physical_memory_kb_count")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_physical_memory_kb_count")
        .register();

    physicalMemoryKbMean = Gauge.build()
        .name("routing_service_status_set_physical_memory_kb_mean")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_physical_memory_kb_mean")
        .register();

    physicalMemoryKbMinimum = Gauge.build()
        .name("routing_service_status_set_physical_memory_kb_minimum")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_physical_memory_kb_minimum")
        .register();

    physicalMemoryKbMaximum = Gauge.build()
        .name("routing_service_status_set_physical_memory_kb_maximum")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_physical_memory_kb_maximum")
        .register();

    physicalMemoryKbStdDev = Gauge.build()
        .name("routing_service_status_set_physical_memory_kb_std_dev")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_physical_memory_kb_std_dev")
        .register();

    totalMemoryKbPeriodMs = Gauge.build()
        .name("routing_service_status_set_total_memory_kb_period_ms")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_total_memory_kb_period_ms")
        .register();

    totalMemoryKbCount = Gauge.build()
        .name("routing_service_status_set_total_memory_kb_count")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_total_memory_kb_count")
        .register();

    totalMemoryKbMean = Gauge.build()
        .name("routing_service_status_set_total_memory_kb_mean")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_total_memory_kb_mean")
        .register();

    totalMemoryKbMinimum = Gauge.build()
        .name("routing_service_status_set_total_memory_kb_minimum")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_total_memory_kb_minimum")
        .register();

    totalMemoryKbMaximum = Gauge.build()
        .name("routing_service_status_set_total_memory_kb_maximum")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_total_memory_kb_maximum")
        .register();

    totalMemoryKbStdDev = Gauge.build()
        .name("routing_service_status_set_total_memory_kb_std_dev")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_total_memory_kb_std_dev")
        .register();

    uptime = Gauge.build()
        .name("routing_service_status_set_uptime")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_uptime")
        .register();

    hostCpuUsagePercentagePeriodMs = Gauge.build()
        .name("routing_service_status_set_host_cpu_usage_percentage_period_ms")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_cpu_usage_percentage_period_ms")
        .register();

    hostCpuUsagePercentageCount = Gauge.build()
        .name("routing_service_status_set_host_cpu_usage_percentage_count")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_cpu_usage_percentage_count")
        .register();

    hostCpuUsagePercentageMean = Gauge.build()
        .name("routing_service_status_set_host_cpu_usage_percentage_mean")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_cpu_usage_percentage_mean")
        .register();

    hostCpuUsagePercentageMinimum = Gauge.build()
        .name("routing_service_status_set_host_cpu_usage_percentage_minimum")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_cpu_usage_percentage_minimum")
        .register();

    hostCpuUsagePercentageMaximum = Gauge.build()
        .name("routing_service_status_set_host_cpu_usage_percentage_maximum")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_cpu_usage_percentage_maximum")
        .register();

    hostCpuUsagePercentageStdDev = Gauge.build()
        .name("routing_service_status_set_host_cpu_usage_percentage_std_dev")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_cpu_usage_percentage_std_dev")
        .register();

    hostFreeMemoryKbPeriodMs = Gauge.build()
        .name("routing_service_status_set_host_free_memory_kb_period_ms")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_free_memory_kb_period_ms")
        .register();

    hostFreeMemoryKbCount = Gauge.build()
        .name("routing_service_status_set_host_free_memory_kb_count")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_free_memory_kb_count")
        .register();

    hostFreeMemoryKbMean = Gauge.build()
        .name("routing_service_status_set_host_free_memory_kb_mean")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_free_memory_kb_mean")
        .register();

    hostFreeMemoryKbMinimum = Gauge.build()
        .name("routing_service_status_set_host_free_memory_kb_minimum")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_free_memory_kb_minimum")
        .register();

    hostFreeMemoryKbMaximum = Gauge.build()
        .name("routing_service_status_set_host_free_memory_kb_maximum")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_free_memory_kb_maximum")
        .register();

    hostFreeMemoryKbStdDev = Gauge.build()
        .name("routing_service_status_set_host_free_memory_kb_std_dev")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_free_memory_kb_std_dev")
        .register();

    hostTotalMemoryKb = Gauge.build()
        .name("routing_service_status_set_host_total_memory_kb")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_total_memory_kb")
        .register();

    hostFreeSwapMemoryKbPeriodMs = Gauge.build()
        .name("routing_service_status_set_host_free_swap_memory_kb_period_ms")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_free_swap_memory_kb_period_ms")
        .register();

    hostFreeSwapMemoryKbCount = Gauge.build()
        .name("routing_service_status_set_host_free_swap_memory_kb_count")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_free_swap_memory_kb_count")
        .register();

    hostFreeSwapMemoryKbMean = Gauge.build()
        .name("routing_service_status_set_host_free_swap_memory_kb_mean")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_free_swap_memory_kb_mean")
        .register();

    hostFreeSwapMemoryKbMinimum = Gauge.build()
        .name("routing_service_status_set_host_free_swap_memory_kb_minimum")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_free_swap_memory_kb_minimum")
        .register();

    hostFreeSwapMemoryKbMaximum = Gauge.build()
        .name("routing_service_status_set_host_free_swap_memory_kb_maximum")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_free_swap_memory_kb_maximum")
        .register();

    hostFreeSwapMemoryKbStdDev = Gauge.build()
        .name("routing_service_status_set_host_free_swap_memory_kb_std_dev")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_free_swap_memory_kb_std_dev")
        .register();

    hostTotalSwapMemoryKb = Gauge.build()
        .name("routing_service_status_set_host_total_swap_memory_kb")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_total_swap_memory_kb")
        .register();

    hostUptime = Gauge.build()
        .name("routing_service_status_set_host_uptime")
        .labelNames(getLabelNames())
        .help("routing_service_status_set_host_uptime")
        .register();
  }

  public void process(
      RoutingServiceStatusSet sample,
      SampleInfo info
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(info.instance_handle, getLabelValues(sample));
    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(info.instance_handle);

    // check if sample is alive and contains valid data
    if (info.instance_state != InstanceStateKind.ALIVE_INSTANCE_STATE || !info.valid_data) {
      // remove labels
      cpuUsagePercentagePeriodMs.remove(labelValues);
      cpuUsagePercentageCount.remove(labelValues);
      cpuUsagePercentageMean.remove(labelValues);
      cpuUsagePercentageMinimum.remove(labelValues);
      cpuUsagePercentageMaximum.remove(labelValues);
      cpuUsagePercentageStdDev.remove(labelValues);
      physicalMemoryKbPeriodMs.remove(labelValues);
      physicalMemoryKbCount.remove(labelValues);
      physicalMemoryKbMean.remove(labelValues);
      physicalMemoryKbMinimum.remove(labelValues);
      physicalMemoryKbMaximum.remove(labelValues);
      physicalMemoryKbStdDev.remove(labelValues);
      totalMemoryKbPeriodMs.remove(labelValues);
      totalMemoryKbCount.remove(labelValues);
      totalMemoryKbMean.remove(labelValues);
      totalMemoryKbMinimum.remove(labelValues);
      totalMemoryKbMaximum.remove(labelValues);
      totalMemoryKbStdDev.remove(labelValues);
      uptime.remove(labelValues);
      hostCpuUsagePercentagePeriodMs.remove(labelValues);
      hostCpuUsagePercentageCount.remove(labelValues);
      hostCpuUsagePercentageMean.remove(labelValues);
      hostCpuUsagePercentageMinimum.remove(labelValues);
      hostCpuUsagePercentageMaximum.remove(labelValues);
      hostCpuUsagePercentageStdDev.remove(labelValues);
      hostFreeMemoryKbPeriodMs.remove(labelValues);
      hostFreeMemoryKbCount.remove(labelValues);
      hostFreeMemoryKbMean.remove(labelValues);
      hostFreeMemoryKbMinimum.remove(labelValues);
      hostFreeMemoryKbMaximum.remove(labelValues);
      hostFreeMemoryKbStdDev.remove(labelValues);
      hostTotalMemoryKb.remove(labelValues);
      hostFreeSwapMemoryKbPeriodMs.remove(labelValues);
      hostFreeSwapMemoryKbCount.remove(labelValues);
      hostFreeSwapMemoryKbMean.remove(labelValues);
      hostFreeSwapMemoryKbMinimum.remove(labelValues);
      hostFreeSwapMemoryKbMaximum.remove(labelValues);
      hostFreeSwapMemoryKbStdDev.remove(labelValues);
      hostTotalSwapMemoryKb.remove(labelValues);
      hostUptime.remove(labelValues);
      // remove instance from hash map
      instanceHandleHashMap.remove(info.instance_handle);
      return;
    }

    // update gauges
    cpuUsagePercentagePeriodMs.labels(labelValues)
        .set(sample.cpu_usage_percentage.publication_period_metrics.period_ms);
    cpuUsagePercentageCount.labels(labelValues)
        .set(sample.cpu_usage_percentage.publication_period_metrics.count);
    cpuUsagePercentageMean.labels(labelValues)
        .set(sample.cpu_usage_percentage.publication_period_metrics.mean);
    cpuUsagePercentageMinimum.labels(labelValues)
        .set(sample.cpu_usage_percentage.publication_period_metrics.minimum);
    cpuUsagePercentageMaximum.labels(labelValues)
        .set(sample.cpu_usage_percentage.publication_period_metrics.maximum);
    cpuUsagePercentageStdDev.labels(labelValues)
        .set(sample.cpu_usage_percentage.publication_period_metrics.std_dev);
    physicalMemoryKbPeriodMs.labels(labelValues)
        .set(sample.physical_memory_kb.publication_period_metrics.period_ms);
    physicalMemoryKbCount.labels(labelValues)
        .set(sample.physical_memory_kb.publication_period_metrics.count);
    physicalMemoryKbMean.labels(labelValues)
        .set(sample.physical_memory_kb.publication_period_metrics.mean);
    physicalMemoryKbMinimum.labels(labelValues)
        .set(sample.physical_memory_kb.publication_period_metrics.minimum);
    physicalMemoryKbMaximum.labels(labelValues)
        .set(sample.physical_memory_kb.publication_period_metrics.maximum);
    physicalMemoryKbStdDev.labels(labelValues)
        .set(sample.physical_memory_kb.publication_period_metrics.std_dev);
    totalMemoryKbPeriodMs.labels(labelValues)
        .set(sample.total_memory_kb.publication_period_metrics.period_ms);
    totalMemoryKbCount.labels(labelValues)
        .set(sample.total_memory_kb.publication_period_metrics.count);
    totalMemoryKbMean.labels(labelValues)
        .set(sample.total_memory_kb.publication_period_metrics.mean);
    totalMemoryKbMinimum.labels(labelValues)
        .set(sample.total_memory_kb.publication_period_metrics.minimum);
    totalMemoryKbMaximum.labels(labelValues)
        .set(sample.total_memory_kb.publication_period_metrics.maximum);
    totalMemoryKbStdDev.labels(labelValues)
        .set(sample.total_memory_kb.publication_period_metrics.std_dev);
    uptime.labels(labelValues)
        .set(sample.uptime);
    hostCpuUsagePercentagePeriodMs.labels(labelValues)
        .set(sample.host_cpu_usage_percentage.publication_period_metrics.period_ms);
    hostCpuUsagePercentageCount.labels(labelValues)
        .set(sample.host_cpu_usage_percentage.publication_period_metrics.count);
    hostCpuUsagePercentageMean.labels(labelValues)
        .set(sample.host_cpu_usage_percentage.publication_period_metrics.mean);
    hostCpuUsagePercentageMinimum.labels(labelValues)
        .set(sample.host_cpu_usage_percentage.publication_period_metrics.minimum);
    hostCpuUsagePercentageMaximum.labels(labelValues)
        .set(sample.host_cpu_usage_percentage.publication_period_metrics.maximum);
    hostCpuUsagePercentageStdDev.labels(labelValues)
        .set(sample.host_cpu_usage_percentage.publication_period_metrics.std_dev);
    hostFreeMemoryKbPeriodMs.labels(labelValues)
        .set(sample.host_free_memory_kb.publication_period_metrics.period_ms);
    hostFreeMemoryKbCount.labels(labelValues)
        .set(sample.host_free_memory_kb.publication_period_metrics.count);
    hostFreeMemoryKbMean.labels(labelValues)
        .set(sample.host_free_memory_kb.publication_period_metrics.mean);
    hostFreeMemoryKbMinimum.labels(labelValues)
        .set(sample.host_free_memory_kb.publication_period_metrics.minimum);
    hostFreeMemoryKbMaximum.labels(labelValues)
        .set(sample.host_free_memory_kb.publication_period_metrics.maximum);
    hostFreeMemoryKbStdDev.labels(labelValues)
        .set(sample.host_free_memory_kb.publication_period_metrics.std_dev);
    hostTotalMemoryKb.labels(labelValues)
        .set(sample.host_total_memory_kb);
    hostFreeSwapMemoryKbPeriodMs.labels(labelValues)
        .set(sample.host_free_swap_memory_kb.publication_period_metrics.period_ms);
    hostFreeSwapMemoryKbCount.labels(labelValues)
        .set(sample.host_free_swap_memory_kb.publication_period_metrics.count);
    hostFreeSwapMemoryKbMean.labels(labelValues)
        .set(sample.host_free_swap_memory_kb.publication_period_metrics.mean);
    hostFreeSwapMemoryKbMinimum.labels(labelValues)
        .set(sample.host_free_swap_memory_kb.publication_period_metrics.minimum);
    hostFreeSwapMemoryKbMaximum.labels(labelValues)
        .set(sample.host_free_swap_memory_kb.publication_period_metrics.maximum);
    hostFreeSwapMemoryKbStdDev.labels(labelValues)
        .set(sample.host_free_swap_memory_kb.publication_period_metrics.std_dev);
    hostTotalSwapMemoryKb.labels(labelValues)
        .set(sample.host_total_swap_memory_kb);
    hostUptime.labels(labelValues)
        .set(sample.host_uptime);
  }

  private String[] getLabelNames() {
    return new String[]{
        "routing_service_name",
    };
  }

  private String[] getLabelValues(
      RoutingServiceStatusSet sample
  ) {
    return new String[]{
        sample.name,
    };
  }
}
