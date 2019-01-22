package com.github.aguther.dds.examples.prometheus.routing;

import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.RTI.RoutingService.Monitoring.RouteStatusSet;
import io.prometheus.client.Gauge;
import java.util.HashMap;

public class RouteStatusSetMetricProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge inputSamplesPerSPeriodMs;
  private final Gauge inputSamplesPerSCount;
  private final Gauge inputSamplesPerSMean;
  private final Gauge inputSamplesPerSMinimum;
  private final Gauge inputSamplesPerSMaximum;
  private final Gauge inputSamplesPerSStdDev;
  private final Gauge inputBytesPerSPeriodMs;
  private final Gauge inputBytesPerSCount;
  private final Gauge inputBytesPerSMean;
  private final Gauge inputBytesPerSMinimum;
  private final Gauge inputBytesPerSMaximum;
  private final Gauge inputBytesPerSStdDev;
  private final Gauge outputSamplesPerSPeriodMs;
  private final Gauge outputSamplesPerSCount;
  private final Gauge outputSamplesPerSMean;
  private final Gauge outputSamplesPerSMinimum;
  private final Gauge outputSamplesPerSMaximum;
  private final Gauge outputSamplesPerSStdDev;
  private final Gauge outputBytesPerSPeriodMs;
  private final Gauge outputBytesPerSCount;
  private final Gauge outputBytesPerSMean;
  private final Gauge outputBytesPerSMinimum;
  private final Gauge outputBytesPerSMaximum;
  private final Gauge outputBytesPerSStdDev;
  private final Gauge latencySPeriodMs;
  private final Gauge latencySCount;
  private final Gauge latencySMean;
  private final Gauge latencySMinimum;
  private final Gauge latencySMaximum;
  private final Gauge latencySStdDev;

  public RouteStatusSetMetricProcessor() {
    instanceHandleHashMap = new HashMap<>();

    inputSamplesPerSPeriodMs = Gauge.build()
        .name("route_status_set_input_samples_per_s_period_ms")
        .labelNames(getLabelNames())
        .help("route_status_set_input_samples_per_s_period_ms")
        .register();

    inputSamplesPerSCount = Gauge.build()
        .name("route_status_set_input_samples_per_s_count")
        .labelNames(getLabelNames())
        .help("route_status_set_input_samples_per_s_count")
        .register();

    inputSamplesPerSMean = Gauge.build()
        .name("route_status_set_input_samples_per_s_mean")
        .labelNames(getLabelNames())
        .help("route_status_set_input_samples_per_s_mean")
        .register();

    inputSamplesPerSMinimum = Gauge.build()
        .name("route_status_set_input_samples_per_s_minimum")
        .labelNames(getLabelNames())
        .help("route_status_set_input_samples_per_s_minimum")
        .register();

    inputSamplesPerSMaximum = Gauge.build()
        .name("route_status_set_input_samples_per_s_maximum")
        .labelNames(getLabelNames())
        .help("route_status_set_input_samples_per_s_maximum")
        .register();

    inputSamplesPerSStdDev = Gauge.build()
        .name("route_status_set_input_samples_per_s_std_dev")
        .labelNames(getLabelNames())
        .help("route_status_set_input_samples_per_s_std_dev")
        .register();

    inputBytesPerSPeriodMs = Gauge.build()
        .name("route_status_set_input_bytes_per_s_period_ms")
        .labelNames(getLabelNames())
        .help("route_status_set_input_bytes_per_s_period_ms")
        .register();

    inputBytesPerSCount = Gauge.build()
        .name("route_status_set_input_bytes_per_s_count")
        .labelNames(getLabelNames())
        .help("route_status_set_input_bytes_per_s_count")
        .register();

    inputBytesPerSMean = Gauge.build()
        .name("route_status_set_input_bytes_per_s_mean")
        .labelNames(getLabelNames())
        .help("route_status_set_input_bytes_per_s_mean")
        .register();

    inputBytesPerSMinimum = Gauge.build()
        .name("route_status_set_input_bytes_per_s_minimum")
        .labelNames(getLabelNames())
        .help("route_status_set_input_bytes_per_s_minimum")
        .register();

    inputBytesPerSMaximum = Gauge.build()
        .name("route_status_set_input_bytes_per_s_maximum")
        .labelNames(getLabelNames())
        .help("route_status_set_input_bytes_per_s_maximum")
        .register();

    inputBytesPerSStdDev = Gauge.build()
        .name("route_status_set_input_bytes_per_s_std_dev")
        .labelNames(getLabelNames())
        .help("route_status_set_input_bytes_per_s_std_dev")
        .register();

    outputSamplesPerSPeriodMs = Gauge.build()
        .name("route_status_set_output_samples_per_s_period_ms")
        .labelNames(getLabelNames())
        .help("route_status_set_output_samples_per_s_period_ms")
        .register();

    outputSamplesPerSCount = Gauge.build()
        .name("route_status_set_output_samples_per_s_count")
        .labelNames(getLabelNames())
        .help("route_status_set_output_samples_per_s_count")
        .register();

    outputSamplesPerSMean = Gauge.build()
        .name("route_status_set_output_samples_per_s_mean")
        .labelNames(getLabelNames())
        .help("route_status_set_output_samples_per_s_mean")
        .register();

    outputSamplesPerSMinimum = Gauge.build()
        .name("route_status_set_output_samples_per_s_minimum")
        .labelNames(getLabelNames())
        .help("route_status_set_output_samples_per_s_minimum")
        .register();

    outputSamplesPerSMaximum = Gauge.build()
        .name("route_status_set_output_samples_per_s_maximum")
        .labelNames(getLabelNames())
        .help("route_status_set_output_samples_per_s_maximum")
        .register();

    outputSamplesPerSStdDev = Gauge.build()
        .name("route_status_set_output_samples_per_s_std_dev")
        .labelNames(getLabelNames())
        .help("route_status_set_output_samples_per_s_std_dev")
        .register();

    outputBytesPerSPeriodMs = Gauge.build()
        .name("route_status_set_output_bytes_per_s_period_ms")
        .labelNames(getLabelNames())
        .help("route_status_set_output_bytes_per_s_period_ms")
        .register();

    outputBytesPerSCount = Gauge.build()
        .name("route_status_set_output_bytes_per_s_count")
        .labelNames(getLabelNames())
        .help("route_status_set_output_bytes_per_s_count")
        .register();

    outputBytesPerSMean = Gauge.build()
        .name("route_status_set_output_bytes_per_s_mean")
        .labelNames(getLabelNames())
        .help("route_status_set_output_bytes_per_s_mean")
        .register();

    outputBytesPerSMinimum = Gauge.build()
        .name("route_status_set_output_bytes_per_s_minimum")
        .labelNames(getLabelNames())
        .help("route_status_set_output_bytes_per_s_minimum")
        .register();

    outputBytesPerSMaximum = Gauge.build()
        .name("route_status_set_output_bytes_per_s_maximum")
        .labelNames(getLabelNames())
        .help("route_status_set_output_bytes_per_s_maximum")
        .register();

    outputBytesPerSStdDev = Gauge.build()
        .name("route_status_set_output_bytes_per_s_std_dev")
        .labelNames(getLabelNames())
        .help("route_status_set_output_bytes_per_s_std_dev")
        .register();

    latencySPeriodMs = Gauge.build()
        .name("route_status_set_latency_s_period_ms")
        .labelNames(getLabelNames())
        .help("route_status_set_latency_s_period_ms")
        .register();

    latencySCount = Gauge.build()
        .name("route_status_set_latency_s_count")
        .labelNames(getLabelNames())
        .help("route_status_set_latency_s_count")
        .register();

    latencySMean = Gauge.build()
        .name("route_status_set_latency_s_mean")
        .labelNames(getLabelNames())
        .help("route_status_set_latency_s_mean")
        .register();

    latencySMinimum = Gauge.build()
        .name("route_status_set_latency_s_minimum")
        .labelNames(getLabelNames())
        .help("route_status_set_latency_s_minimum")
        .register();

    latencySMaximum = Gauge.build()
        .name("route_status_set_latency_s_maximum")
        .labelNames(getLabelNames())
        .help("route_status_set_latency_s_maximum")
        .register();

    latencySStdDev = Gauge.build()
        .name("route_status_set_latency_s_std_dev")
        .labelNames(getLabelNames())
        .help("route_status_set_latency_s_std_dev")
        .register();
  }

  public void process(
      RouteStatusSet sample,
      SampleInfo info
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(info.instance_handle, getLabelValues(sample));
    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(info.instance_handle);

    // check if sample is alive and contains valid data
    if (info.instance_state != InstanceStateKind.ALIVE_INSTANCE_STATE || !info.valid_data) {
      // remove labels
      inputSamplesPerSPeriodMs.remove(labelValues);
      inputSamplesPerSCount.remove(labelValues);
      inputSamplesPerSMean.remove(labelValues);
      inputSamplesPerSMinimum.remove(labelValues);
      inputSamplesPerSMaximum.remove(labelValues);
      inputSamplesPerSStdDev.remove(labelValues);
      inputBytesPerSPeriodMs.remove(labelValues);
      inputBytesPerSCount.remove(labelValues);
      inputBytesPerSMean.remove(labelValues);
      inputBytesPerSMinimum.remove(labelValues);
      inputBytesPerSMaximum.remove(labelValues);
      inputBytesPerSStdDev.remove(labelValues);
      outputSamplesPerSPeriodMs.remove(labelValues);
      outputSamplesPerSCount.remove(labelValues);
      outputSamplesPerSMean.remove(labelValues);
      outputSamplesPerSMinimum.remove(labelValues);
      outputSamplesPerSMaximum.remove(labelValues);
      outputSamplesPerSStdDev.remove(labelValues);
      outputBytesPerSPeriodMs.remove(labelValues);
      outputBytesPerSCount.remove(labelValues);
      outputBytesPerSMean.remove(labelValues);
      outputBytesPerSMinimum.remove(labelValues);
      outputBytesPerSMaximum.remove(labelValues);
      outputBytesPerSStdDev.remove(labelValues);
      latencySPeriodMs.remove(labelValues);
      latencySCount.remove(labelValues);
      latencySMean.remove(labelValues);
      latencySMinimum.remove(labelValues);
      latencySMaximum.remove(labelValues);
      latencySStdDev.remove(labelValues);
      // remove instance from hash map
      instanceHandleHashMap.remove(info.instance_handle);
      return;
    }

    // update gauges
    inputSamplesPerSPeriodMs.labels(labelValues)
        .set(sample.input_samples_per_s.publication_period_metrics.period_ms);
    inputSamplesPerSCount.labels(labelValues)
        .set(sample.input_samples_per_s.publication_period_metrics.count);
    inputSamplesPerSMean.labels(labelValues)
        .set(sample.input_samples_per_s.publication_period_metrics.mean);
    inputSamplesPerSMinimum.labels(labelValues)
        .set(sample.input_samples_per_s.publication_period_metrics.minimum);
    inputSamplesPerSMaximum.labels(labelValues)
        .set(sample.input_samples_per_s.publication_period_metrics.maximum);
    inputSamplesPerSStdDev.labels(labelValues)
        .set(sample.input_samples_per_s.publication_period_metrics.std_dev);
    inputBytesPerSPeriodMs.labels(labelValues)
        .set(sample.input_bytes_per_s.publication_period_metrics.period_ms);
    inputBytesPerSCount.labels(labelValues)
        .set(sample.input_bytes_per_s.publication_period_metrics.count);
    inputBytesPerSMean.labels(labelValues)
        .set(sample.input_bytes_per_s.publication_period_metrics.mean);
    inputBytesPerSMinimum.labels(labelValues)
        .set(sample.input_bytes_per_s.publication_period_metrics.minimum);
    inputBytesPerSMaximum.labels(labelValues)
        .set(sample.input_bytes_per_s.publication_period_metrics.maximum);
    inputBytesPerSStdDev.labels(labelValues)
        .set(sample.input_bytes_per_s.publication_period_metrics.std_dev);
    outputSamplesPerSPeriodMs.labels(labelValues)
        .set(sample.output_samples_per_s.publication_period_metrics.period_ms);
    outputSamplesPerSCount.labels(labelValues)
        .set(sample.output_samples_per_s.publication_period_metrics.count);
    outputSamplesPerSMean.labels(labelValues)
        .set(sample.output_samples_per_s.publication_period_metrics.mean);
    outputSamplesPerSMinimum.labels(labelValues)
        .set(sample.output_samples_per_s.publication_period_metrics.minimum);
    outputSamplesPerSMaximum.labels(labelValues)
        .set(sample.output_samples_per_s.publication_period_metrics.maximum);
    outputSamplesPerSStdDev.labels(labelValues)
        .set(sample.output_samples_per_s.publication_period_metrics.std_dev);
    outputBytesPerSPeriodMs.labels(labelValues)
        .set(sample.output_bytes_per_s.publication_period_metrics.period_ms);
    outputBytesPerSCount.labels(labelValues)
        .set(sample.output_bytes_per_s.publication_period_metrics.count);
    outputBytesPerSMean.labels(labelValues)
        .set(sample.output_bytes_per_s.publication_period_metrics.mean);
    outputBytesPerSMinimum.labels(labelValues)
        .set(sample.output_bytes_per_s.publication_period_metrics.minimum);
    outputBytesPerSMaximum.labels(labelValues)
        .set(sample.output_bytes_per_s.publication_period_metrics.maximum);
    outputBytesPerSStdDev.labels(labelValues)
        .set(sample.output_bytes_per_s.publication_period_metrics.std_dev);
    latencySPeriodMs.labels(labelValues)
        .set(sample.latency_s.publication_period_metrics.period_ms);
    latencySCount.labels(labelValues)
        .set(sample.latency_s.publication_period_metrics.count);
    latencySMean.labels(labelValues)
        .set(sample.latency_s.publication_period_metrics.mean);
    latencySMinimum.labels(labelValues)
        .set(sample.latency_s.publication_period_metrics.minimum);
    latencySMaximum.labels(labelValues)
        .set(sample.latency_s.publication_period_metrics.maximum);
    latencySStdDev.labels(labelValues)
        .set(sample.latency_s.publication_period_metrics.std_dev);
  }

  private String[] getLabelNames() {
    return new String[]{
        "routing_service_name",
        "domain_route_name",
        "session_name",
        "route_name",
    };
  }

  private String[] getLabelValues(
      RouteStatusSet sample
  ) {
    return new String[]{
        sample.routing_service_name,
        sample.domain_route_name,
        sample.session_name,
        sample.name,
    };
  }
}
