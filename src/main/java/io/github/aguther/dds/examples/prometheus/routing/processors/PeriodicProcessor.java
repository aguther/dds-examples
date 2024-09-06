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

import static com.google.common.base.Preconditions.checkNotNull;

import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import idl.RTI.Service.Monitoring.Config;
import idl.RTI.Service.Monitoring.Event;
import idl.RTI.Service.Monitoring.Periodic;
import idl.RTI.Service.Monitoring.ResourceGuid;
import idl.RTI.Service.Monitoring.ResourceKind;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PeriodicProcessor {

  private static final Logger LOGGER = LogManager.getLogger(PeriodicProcessor.class);

  private ConfigEventProcessorCache configEventProcessorCache;
  private HashMap<InstanceHandle_t, ResourceGuid> instanceHandleResourceGuidHashMap;

  private RoutingServiceProcessor routingServiceProcessor;
  private DomainRouteProcessor domainRouteProcessor;
  private SessionProcessor sessionProcessor;
  private AutoRouteProcessor autoRouteProcessor;
  private RouteProcessor routeProcessor;
  private InputProcessor inputProcessor;
  private OutputProcessor outputProcessor;

  public PeriodicProcessor(
    ConfigEventProcessorCache configEventProcessorCache
  ) {
    // check and store config and event processor cache
    checkNotNull(configEventProcessorCache);
    this.configEventProcessorCache = configEventProcessorCache;
    // create hash map to store instance handles to resource guid
    instanceHandleResourceGuidHashMap = new HashMap<>();
    // create processors
    routingServiceProcessor = new RoutingServiceProcessor();
    domainRouteProcessor = new DomainRouteProcessor();
    sessionProcessor = new SessionProcessor();
    autoRouteProcessor = new AutoRouteProcessor();
    routeProcessor = new RouteProcessor();
    inputProcessor = new InputProcessor();
    outputProcessor = new OutputProcessor();
  }

  public void process(
    Periodic sample,
    SampleInfo info
  ) {
    if ((info.instance_state == InstanceStateKind.ALIVE_INSTANCE_STATE) && (info.valid_data)) {
      processValid(sample, info);
    } else {
      processRemove(info);
    }
  }

  private void processValid(
    Periodic sample,
    SampleInfo info
  ) {
    // add instance to hash map
    instanceHandleResourceGuidHashMap.putIfAbsent(info.instance_handle, sample.object_guid);

    // get matching config
    Config config = configEventProcessorCache.getConfig(sample.object_guid);
    if (config == null) {
      return;
    }

    // get matching event
    Event event = configEventProcessorCache.getEvent(sample.object_guid);
    if (event == null) {
      return;
    }

    // process sample
    if (ResourceKind.ROUTING_SERVICE.equals(sample.value._d)) {
      routingServiceProcessor.processAddUpdate(
        info.instance_handle,
        config.value.routing_service,
        event.value.routing_service,
        sample.value.routing_service
      );
    }
    if (ResourceKind.ROUTING_DOMAIN_ROUTE.equals(sample.value._d)) {
      domainRouteProcessor.processAddUpdate(
        info.instance_handle,
        config.value.routing_domain_route,
        event.value.routing_domain_route,
        sample.value.routing_domain_route
      );
    }
    if (ResourceKind.ROUTING_SESSION.equals(sample.value._d)) {
      sessionProcessor.processAddUpdate(
        info.instance_handle,
        config.value.routing_session,
        event.value.routing_session,
        sample.value.routing_session
      );
    }
    if (ResourceKind.ROUTING_AUTO_ROUTE.equals(sample.value._d)) {
      autoRouteProcessor.processAddUpdate(
        info.instance_handle,
        config.value.routing_auto_route,
        event.value.routing_auto_route,
        sample.value.routing_auto_route
      );
    }
    if (ResourceKind.ROUTING_ROUTE.equals(sample.value._d)) {
      routeProcessor.processAddUpdate(
        info.instance_handle,
        config.value.routing_route,
        event.value.routing_route,
        sample.value.routing_route
      );
    }
    if (ResourceKind.ROUTING_INPUT.equals(sample.value._d)) {
      inputProcessor.processAddUpdate(
        info.instance_handle,
        config.value.routing_input,
        event.value.routing_input,
        sample.value.routing_input
      );
    }
    if (ResourceKind.ROUTING_OUTPUT.equals(sample.value._d)) {
      outputProcessor.processAddUpdate(
        info.instance_handle,
        config.value.routing_output,
        event.value.routing_output,
        sample.value.routing_output
      );
    }
  }

  private void processRemove(
    SampleInfo info
  ) {
    // get and remove resource guid
    ResourceGuid resourceGuid = instanceHandleResourceGuidHashMap.remove(info.instance_handle);

    // get matching config
    Config config = configEventProcessorCache.getConfig(resourceGuid);
    if (config == null) {
      return;
    }

    // determine type of object and process remove
    if (ResourceKind.ROUTING_SERVICE.equals(config.value._d)) {
      routingServiceProcessor.processRemove(info.instance_handle);
    }
    if (ResourceKind.ROUTING_DOMAIN_ROUTE.equals(config.value._d)) {
      domainRouteProcessor.processRemove(info.instance_handle);
    }
    if (ResourceKind.ROUTING_SESSION.equals(config.value._d)) {
      sessionProcessor.processRemove(info.instance_handle);
    }
    if (ResourceKind.ROUTING_AUTO_ROUTE.equals(config.value._d)) {
      autoRouteProcessor.processRemove(info.instance_handle);
    }
    if (ResourceKind.ROUTING_ROUTE.equals(config.value._d)) {
      routeProcessor.processRemove(info.instance_handle);
    }
    if (ResourceKind.ROUTING_INPUT.equals(config.value._d)) {
      inputProcessor.processRemove(info.instance_handle);
    }
    if (ResourceKind.ROUTING_OUTPUT.equals(config.value._d)) {
      outputProcessor.processRemove(info.instance_handle);
    }
  }
}
