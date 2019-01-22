package com.github.aguther.dds.examples.prometheus.monitoring;

import com.github.aguther.dds.util.BuiltinTopicHelper;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.rti.dds.monitoring.DomainParticipantDescription;
import io.prometheus.client.Gauge;
import java.util.HashMap;

public class DomainParticipantDescriptionMetricProcessor {

  private final HashMap<InstanceHandle_t, String[]> instanceHandleHashMap;

  private final Gauge dummy;

  public DomainParticipantDescriptionMetricProcessor() {
    instanceHandleHashMap = new HashMap<>();

    dummy = Gauge.build()
        .name("domainparticipant_description")
        .labelNames(getLabelNames())
        .help("domainparticipant_description")
        .register();
  }

  public void process(
      DomainParticipantDescription sample,
      SampleInfo info
  ) {
    // put instance handle to hash map if not present
    instanceHandleHashMap.putIfAbsent(info.instance_handle, getLabelValues(sample));
    // get label values once to improve performance
    final String[] labelValues = instanceHandleHashMap.get(info.instance_handle);

    // check if sample is alive and contains valid data
    if (info.instance_state != InstanceStateKind.ALIVE_INSTANCE_STATE || !info.valid_data) {
      // remove labels
      dummy.remove(labelValues);
      // remove instance from hash map
      instanceHandleHashMap.remove(info.instance_handle);
      return;
    }

    // update gauges
    dummy.labels(labelValues).set(1);
  }

  private String[] getLabelNames() {
    return new String[]{
        "participant_key",
        "domain_id",
        "host_id",
        "process_id",
        "participant_name",
        "participant_role_name"
    };
  }

  private String[] getLabelValues(
      DomainParticipantDescription sample
  ) {
    return new String[]{
        BuiltinTopicHelper.toString(sample.entity_key.value),
        Integer.toUnsignedString(sample.domain_id),
        Integer.toUnsignedString(sample.host_id),
        Integer.toUnsignedString(sample.process_id),
        sample.qos.participant_name.name,
        sample.qos.participant_name.role_name
    };
  }
}
