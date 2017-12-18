package com.github.aguther.dds.examples.discovery;

import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;

public interface PublicationObserverListener {

  public void publicationDiscovered(
      InstanceHandle_t instanceHandle,
      PublicationBuiltinTopicData data
  );

  public void publicationLost(
      InstanceHandle_t instanceHandle,
      PublicationBuiltinTopicData data
  );
}
