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

package com.github.aguther.dds.examples.prometheus.monitoring;

import com.github.aguther.dds.util.BuiltinTopicHelper;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.DDSMonitoring.Duration_t;
import idl.rti.dds.monitoring.DomainParticipantEntityStatistics;
import io.prometheus.client.Gauge;
import java.util.HashMap;

public class DomainParticipantEntityStatisticsMetricProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge userCpuTime;
  private final Gauge kernelCpuTime;
  private final Gauge physicalMemoryBytes;
  private final Gauge totalMemoryBytes;
  private final Gauge remoteParticipantCount;
  private final Gauge remoteWriterCount;
  private final Gauge remoteReaderCount;

  public DomainParticipantEntityStatisticsMetricProcessor() {
    instanceHandleHashMap = new HashMap<>();

    userCpuTime = Gauge.build()
      .name("domainparticipant_entity_statistics_user_cpu_time")
      .labelNames(getLabelNames())
      .help("domainparticipant_entity_statistics_user_cpu_time")
      .register();

    kernelCpuTime = Gauge.build()
      .name("domainparticipant_entity_statistics_kernel_cpu_time")
      .labelNames(getLabelNames())
      .help("domainparticipant_entity_statistics_kernel_cpu_time")
      .register();

    physicalMemoryBytes = Gauge.build()
      .name("domainparticipant_entity_statistics_physical_memory_bytes")
      .labelNames(getLabelNames())
      .help("domainparticipant_entity_statistics_physical_memory_bytes")
      .register();

    totalMemoryBytes = Gauge.build()
      .name("domainparticipant_entity_statistics_total_memory_bytes")
      .labelNames(getLabelNames())
      .help("domainparticipant_entity_statistics_total_memory_bytes")
      .register();

    remoteParticipantCount = Gauge.build()
      .name("domainparticipant_entity_statistics_remote_participant_count")
      .labelNames(getLabelNames())
      .help("domainparticipant_entity_statistics_remote_participant_count")
      .register();

    remoteWriterCount = Gauge.build()
      .name("domainparticipant_entity_statistics_remote_writer_count")
      .labelNames(getLabelNames())
      .help("domainparticipant_entity_statistics_remote_writer_count")
      .register();

    remoteReaderCount = Gauge.build()
      .name("domainparticipant_entity_statistics_remote_reader_count")
      .labelNames(getLabelNames())
      .help("domainparticipant_entity_statistics_remote_reader_count")
      .register();

  }

  public void process(
    DomainParticipantEntityStatistics sample,
    SampleInfo info
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(info.instance_handle, getLabelValues(sample));
    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(info.instance_handle);

    // check if sample is alive and contains valid data
    if (info.instance_state != InstanceStateKind.ALIVE_INSTANCE_STATE || !info.valid_data) {
      // remove labels
      userCpuTime.remove(labelValues);
      kernelCpuTime.remove(labelValues);
      physicalMemoryBytes.remove(labelValues);
      totalMemoryBytes.remove(labelValues);
      remoteParticipantCount.remove(labelValues);
      remoteWriterCount.remove(labelValues);
      remoteReaderCount.remove(labelValues);
      // remove instance from hash map
      instanceHandleHashMap.remove(info.instance_handle);
      return;
    }

    // update gauges
    userCpuTime.labels(labelValues).set(getDurationNanosec(sample.process.user_cpu_time));
    kernelCpuTime.labels(labelValues).set(getDurationNanosec(sample.process.kernel_cpu_time));
    physicalMemoryBytes.labels(labelValues).set(sample.process.physical_memory_bytes);
    totalMemoryBytes.labels(labelValues).set(sample.process.total_memory_bytes);
    remoteParticipantCount.labels(labelValues).set(sample.remoteParticipantCount);
    remoteWriterCount.labels(labelValues).set(sample.remoteWriterCount);
    remoteReaderCount.labels(labelValues).set(sample.remoteReaderCount);
  }

  private String[] getLabelNames() {
    return new String[]{
      "participant_key",
      "period",
      "domain_id",
      "host_id",
      "process_id"
    };
  }

  private String[] getLabelValues(
    DomainParticipantEntityStatistics sample
  ) {
    return new String[]{
      BuiltinTopicHelper.toString(sample.participant_key.value),
      Long.toUnsignedString((long) sample.period.sec * 1000000000 + (long) sample.period.nanosec),
      Integer.toUnsignedString(sample.domain_id),
      Integer.toUnsignedString(sample.host_id),
      Integer.toUnsignedString(sample.process_id),
    };
  }

  private long getDurationNanosec(
    Duration_t duration
  ) {
    return ((long) duration.sec * 1000000000) + (long) duration.nanosec;
  }
}
