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

import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.DDSMonitoring.BuiltinTopicKey_t;
import idl.rti.dds.monitoring.DataReaderDescription;
import idl.rti.dds.monitoring.DataWriterDescription;
import idl.rti.dds.monitoring.DomainParticipantDescription;
import idl.rti.dds.monitoring.PublisherDescription;
import idl.rti.dds.monitoring.SubscriberDescription;
import idl.rti.dds.monitoring.TopicDescription;
import java.util.HashMap;

public class DescriptionProcessorCache {

  private HashMap<BuiltinTopicKey_t, DomainParticipantDescription> domainParticipantDescriptionStore;
  private HashMap<InstanceHandle_t, BuiltinTopicKey_t> domainParticipantDescriptionMapping;

  private HashMap<BuiltinTopicKey_t, TopicDescription> topicDescriptionStore;
  private HashMap<InstanceHandle_t, BuiltinTopicKey_t> topicDescriptionMapping;

  private HashMap<BuiltinTopicKey_t, PublisherDescription> publisherDescriptionStore;
  private HashMap<InstanceHandle_t, BuiltinTopicKey_t> publisherDescriptionMapping;

  private HashMap<BuiltinTopicKey_t, DataWriterDescription> dataWriterDescriptionStore;
  private HashMap<InstanceHandle_t, BuiltinTopicKey_t> dataWriterDescriptionMapping;

  private HashMap<BuiltinTopicKey_t, SubscriberDescription> subscriberDescriptionStore;
  private HashMap<InstanceHandle_t, BuiltinTopicKey_t> subscriberDescriptionMapping;

  private HashMap<BuiltinTopicKey_t, DataReaderDescription> dataReaderDescriptionStore;
  private HashMap<InstanceHandle_t, BuiltinTopicKey_t> dataReaderDescriptionMapping;

  public DescriptionProcessorCache() {
    domainParticipantDescriptionStore = new HashMap<>();
    domainParticipantDescriptionMapping = new HashMap<>();

    topicDescriptionStore = new HashMap<>();
    topicDescriptionMapping = new HashMap<>();

    publisherDescriptionStore = new HashMap<>();
    publisherDescriptionMapping = new HashMap<>();

    dataWriterDescriptionStore = new HashMap<>();
    dataWriterDescriptionMapping = new HashMap<>();

    subscriberDescriptionStore = new HashMap<>();
    subscriberDescriptionMapping = new HashMap<>();

    dataReaderDescriptionStore = new HashMap<>();
    dataReaderDescriptionMapping = new HashMap<>();
  }

  public synchronized void process(
    DomainParticipantDescription sample,
    SampleInfo info
  ) {
    process(
      sample,
      info,
      sample.entity_key,
      domainParticipantDescriptionStore,
      domainParticipantDescriptionMapping
    );
  }

  public synchronized void process(
    TopicDescription sample,
    SampleInfo info
  ) {
    process(
      sample,
      info,
      sample.entity_key,
      topicDescriptionStore,
      topicDescriptionMapping
    );
  }

  public synchronized void process(
    PublisherDescription sample,
    SampleInfo info
  ) {
    process(
      sample,
      info,
      sample.entity_key,
      publisherDescriptionStore,
      publisherDescriptionMapping
    );
  }

  public synchronized void process(
    DataWriterDescription sample,
    SampleInfo info
  ) {
    process(
      sample,
      info,
      sample.entity_key,
      dataWriterDescriptionStore,
      dataWriterDescriptionMapping
    );
  }

  public synchronized void process(
    SubscriberDescription sample,
    SampleInfo info
  ) {
    process(
      sample,
      info,
      sample.entity_key,
      subscriberDescriptionStore,
      subscriberDescriptionMapping
    );
  }

  public synchronized void process(
    DataReaderDescription sample,
    SampleInfo info
  ) {
    process(
      sample,
      info,
      sample.entity_key,
      dataReaderDescriptionStore,
      dataReaderDescriptionMapping
    );
  }

  public DomainParticipantDescription getDomainParticipantDescription(
    BuiltinTopicKey_t key
  ) {
    return domainParticipantDescriptionStore.get(key);
  }

  public TopicDescription getTopicDescription(
    BuiltinTopicKey_t key
  ) {
    return topicDescriptionStore.get(key);
  }

  public PublisherDescription getPublisherDescription(
    BuiltinTopicKey_t key
  ) {
    return publisherDescriptionStore.get(key);
  }

  public DataWriterDescription getDataWriterDescription(
    BuiltinTopicKey_t key
  ) {
    return dataWriterDescriptionStore.get(key);
  }

  public SubscriberDescription getSubscriberDescription(
    BuiltinTopicKey_t key
  ) {
    return subscriberDescriptionStore.get(key);
  }

  public DataReaderDescription getDataReaderDescription(
    BuiltinTopicKey_t key
  ) {
    return dataReaderDescriptionStore.get(key);
  }

  private <T> void process(
    T sample,
    SampleInfo info,
    BuiltinTopicKey_t topicKey,
    HashMap<BuiltinTopicKey_t, T> store,
    HashMap<InstanceHandle_t, BuiltinTopicKey_t> mapping
  ) {
    // check if sample is alive and contains valid data
    if (info.instance_state == InstanceStateKind.ALIVE_INSTANCE_STATE && info.valid_data) {
      // put instance handle to hash map if not present
      store.putIfAbsent(topicKey, sample);
      mapping.putIfAbsent(info.instance_handle, topicKey);
    } else {
      // get builtin topic key and remove it from the mapping
      BuiltinTopicKey_t key = mapping.remove(info.instance_handle);
      // remove the description if necessary
      if (key != null) {
        store.remove(key);
      }
    }
  }
}
