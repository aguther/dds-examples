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

import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.RTI.Service.Monitoring.Config;
import idl.RTI.Service.Monitoring.Event;
import idl.RTI.Service.Monitoring.ResourceGuid;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigEventProcessorCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigEventProcessorCache.class);

  private HashMap<ResourceGuid, Config> configStore;
  private HashMap<ResourceGuid, Event> eventStore;

  public ConfigEventProcessorCache() {
    configStore = new HashMap<>();
    eventStore = new HashMap<>();
  }

  public synchronized void process(
    Config sample,
    SampleInfo info
  ) {
    process(
      sample,
      info,
      sample.object_guid,
      configStore
    );
  }

  public synchronized void process(
    Event sample,
    SampleInfo info
  ) {
    process(
      sample,
      info,
      sample.object_guid,
      eventStore
    );
  }

  Config getConfig(
    ResourceGuid key
  ) {
    return configStore.get(key);
  }

  Event getEvent(
    ResourceGuid key
  ) {
    return eventStore.get(key);
  }

  private <T> void process(
    T sample,
    SampleInfo info,
    ResourceGuid key,
    HashMap<ResourceGuid, T> store
  ) {
    // check if sample is alive and contains valid data
    if (info.instance_state == InstanceStateKind.ALIVE_INSTANCE_STATE && info.valid_data) {
      // put instance handle to hash map if not present
      store.putIfAbsent(key, sample);
    } else {
      // remove the description if necessary
      store.remove(key);
    }
  }
}
