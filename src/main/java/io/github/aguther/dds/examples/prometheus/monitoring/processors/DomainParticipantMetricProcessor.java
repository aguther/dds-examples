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

package io.github.aguther.dds.examples.prometheus.monitoring.processors;

import static com.google.common.base.Preconditions.checkNotNull;

import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.DDSMonitoring.Duration_t;
import idl.rti.dds.monitoring.DomainParticipantDescription;
import idl.rti.dds.monitoring.DomainParticipantEntityStatistics;
import io.github.aguther.dds.util.BuiltinTopicHelper;
import io.prometheus.client.Gauge;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class DomainParticipantMetricProcessor {

  private final DescriptionProcessorCache descriptionProcessorCache;
  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge userCpuTime;
  private final Gauge kernelCpuTime;
  private final Gauge physicalMemoryBytes;
  private final Gauge totalMemoryBytes;
  private final Gauge remoteParticipantCount;
  private final Gauge remoteWriterCount;
  private final Gauge remoteReaderCount;

  public DomainParticipantMetricProcessor(
    DescriptionProcessorCache descriptionProcessorCache
  ) {
    checkNotNull(descriptionProcessorCache);
    this.descriptionProcessorCache = descriptionProcessorCache;
    instanceHandleHashMap = new HashMap<>();

    userCpuTime = Gauge.build()
      .name("dds_domain_participant_user_cpu_time_nanoseconds")
      .labelNames(getLabelNames())
      .help("dds_domain_participant_user_cpu_time_nanoseconds")
      .register();

    kernelCpuTime = Gauge.build()
      .name("dds_domain_participant_kernel_cpu_time_nanoseconds")
      .labelNames(getLabelNames())
      .help("dds_domain_participant_kernel_cpu_time_nanoseconds")
      .register();

    physicalMemoryBytes = Gauge.build()
      .name("dds_domain_participant_physical_memory_bytes")
      .labelNames(getLabelNames())
      .help("dds_domain_participant_physical_memory_bytes")
      .register();

    totalMemoryBytes = Gauge.build()
      .name("dds_domain_participant_total_memory_bytes")
      .labelNames(getLabelNames())
      .help("dds_domain_participant_total_memory_bytes")
      .register();

    remoteParticipantCount = Gauge.build()
      .name("dds_domain_participant_remote_participant_count")
      .labelNames(getLabelNames())
      .help("dds_domain_participant_remote_participant_count")
      .register();

    remoteWriterCount = Gauge.build()
      .name("dds_domain_participant_remote_writer_count")
      .labelNames(getLabelNames())
      .help("dds_domain_participant_remote_writer_count")
      .register();

    remoteReaderCount = Gauge.build()
      .name("dds_domain_participant_remote_reader_count")
      .labelNames(getLabelNames())
      .help("dds_domain_participant_remote_reader_count")
      .register();

  }

  public void process(
    DomainParticipantEntityStatistics sample,
    SampleInfo info
  ) {
    if ((info.instance_state == InstanceStateKind.ALIVE_INSTANCE_STATE)
      && (info.valid_data)
      && (descriptionProcessorCache.getDomainParticipantDescription(sample.participant_key) != null)) {
      // add / update values
      addUpdateGaugesForLabel(info.instance_handle, sample);
    } else {
      // remove values
      removeLabelsFromGauges(info.instance_handle);
    }
  }

  private void addUpdateGaugesForLabel(
    InstanceHandle_t instanceHandle,
    DomainParticipantEntityStatistics sample
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(
      instanceHandle,
      getLabelValues(descriptionProcessorCache.getDomainParticipantDescription(sample.participant_key), sample)
    );

    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(instanceHandle);

    // update gauges
    userCpuTime.labels(labelValues).set(getDurationNanoseconds(sample.process.user_cpu_time));
    kernelCpuTime.labels(labelValues).set(getDurationNanoseconds(sample.process.kernel_cpu_time));
    physicalMemoryBytes.labels(labelValues).set(sample.process.physical_memory_bytes);
    totalMemoryBytes.labels(labelValues).set(sample.process.total_memory_bytes);
    remoteParticipantCount.labels(labelValues).set(sample.remoteParticipantCount);
    remoteWriterCount.labels(labelValues).set(sample.remoteWriterCount);
    remoteReaderCount.labels(labelValues).set(sample.remoteReaderCount);
  }

  private void removeLabelsFromGauges(
    InstanceHandle_t instanceHandle
  ) {
    // check if remove is necessary
    if (!instanceHandleHashMap.containsKey(instanceHandle)) {
      return;
    }

    // get label values
    final String[] labelValues = instanceHandleHashMap.get(instanceHandle);

    // remove labels
    userCpuTime.remove(labelValues);
    kernelCpuTime.remove(labelValues);
    physicalMemoryBytes.remove(labelValues);
    totalMemoryBytes.remove(labelValues);
    remoteParticipantCount.remove(labelValues);
    remoteWriterCount.remove(labelValues);
    remoteReaderCount.remove(labelValues);

    // remove instance from hash map
    instanceHandleHashMap.remove(instanceHandle);
  }

  private String[] getLabelNames() {
    return new String[]{
      "participant_key",
      "domain_id",
      "host_id",
      "process_id",
      "participant_name",
      "participant_role_name",
    };
  }

  private String[] getLabelValues(
    DomainParticipantDescription description,
    DomainParticipantEntityStatistics statistics
  ) {
    return new String[]{
      BuiltinTopicHelper.toString(statistics.participant_key.value),
      Integer.toUnsignedString(statistics.domain_id),
      Integer.toUnsignedString(statistics.host_id),
      Integer.toUnsignedString(statistics.process_id),
      description.qos.participant_name.name,
      description.qos.participant_name.role_name,
    };
  }

  private long getDurationNanoseconds(
    Duration_t duration
  ) {
    return (TimeUnit.SECONDS.toNanos(duration.sec) + duration.nanosec);
  }
}
