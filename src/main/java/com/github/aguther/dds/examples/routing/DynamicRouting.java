/*
 * MIT License
 *
 * Copyright (c) 2017 Andreas Guther
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

package com.github.aguther.dds.examples.routing;

import com.github.aguther.dds.discovery.observer.PublicationObserver;
import com.github.aguther.dds.discovery.observer.SubscriptionObserver;
import com.github.aguther.dds.routing.dynamic.command.DynamicPartitionCommander;
import com.github.aguther.dds.routing.dynamic.command.DynamicPartitionCommanderProviderImpl;
import com.github.aguther.dds.routing.dynamic.observer.DynamicPartitionObserver;
import com.github.aguther.dds.routing.dynamic.observer.filter.RoutingServiceEntitiesFilter;
import com.github.aguther.dds.routing.dynamic.observer.filter.RoutingServiceGroupEntitiesFilter;
import com.github.aguther.dds.routing.dynamic.observer.filter.RtiTopicFilter;
import com.github.aguther.dds.routing.dynamic.observer.filter.WildcardPartitionFilter;
import com.github.aguther.dds.util.AutoEnableCreatedEntitiesHelper;
import com.github.aguther.dds.util.RoutingServiceCommandHelper;
import com.github.aguther.dds.logging.Slf4jDdsLogger;
import com.google.common.util.concurrent.AbstractIdleService;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.ServiceQosPolicyKind;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.routingservice.RoutingService;
import com.rti.routingservice.RoutingServiceProperty;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicRouting extends AbstractIdleService {

  private static final String ROUTING_SERVICE_NAME;
  private static final String ROUTING_SERVICE_CONFIG_FILE;
  private static final Logger log;

  private static DynamicRouting serviceInstance;

  private RoutingService routingService;

  private DomainParticipant domainParticipantAdministration;
  private DomainParticipant domainParticipantDiscovery;
  private PublicationObserver publicationObserver;
  private SubscriptionObserver subscriptionObserver;
  private DynamicPartitionObserver dynamicPartitionObserver;
  private DynamicPartitionCommander dynamicPartitionCommander;
  private RoutingServiceCommandHelper routingServiceCommandHelper;

  static {
    ROUTING_SERVICE_NAME = "dds-examples-routing-dynamic";
    ROUTING_SERVICE_CONFIG_FILE = "routing-dynamic.xml";
    log = LoggerFactory.getLogger(DynamicRouting.class);
  }

  public static void main(
      String[] args
  ) {
    // register shutdown hook
    registerShutdownHook();

    // create service
    serviceInstance = new DynamicRouting();

    // start the service
    serviceInstance.startAsync();

    // wait for termination
    serviceInstance.awaitTerminated();

    // service terminated
    log.info("Service terminated");
  }

  private static void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Shutdown signal received");
      if (serviceInstance != null) {
        serviceInstance.stopAsync();
        serviceInstance.awaitTerminated();
      }
      log.info("Shutdown signal finished");
    }));
  }

  @Override
  protected void startUp() throws Exception {
    // log service start
    log.info("Service is starting");

    // start DDS logger
    startUpDdsLogger();

    // start routing service
    startUpRoutingService();

    // start dynamic routing
    startUpDynamicRouting();

    // log service start
    log.info("Service start finished");
  }

  @Override
  protected void shutDown() throws Exception {
    // log service start
    log.info("Service is shutting down");

    // shutdown dynamic routing
    shutdownDynamicRouting();

    // shutdown routing service
    shutdownRoutingService();

    // log service start
    log.info("Service shutdown finished");
  }

  private void startUpDdsLogger() throws IOException {
    Slf4jDdsLogger.createRegisterLogger();
  }

  private void startUpRoutingService() {
    // setup routing service properties
    final RoutingServiceProperty routingServiceProperty = new RoutingServiceProperty();
    routingServiceProperty.cfgFile = ROUTING_SERVICE_CONFIG_FILE;
    routingServiceProperty.serviceName = ROUTING_SERVICE_NAME;
    routingServiceProperty.applicationName = routingServiceProperty.serviceName;
    routingServiceProperty.serviceVerbosity = 3;

    // create routing service instance
    routingService = new RoutingService(routingServiceProperty);

    // start routing service
    routingService.start();
  }

  private void shutdownRoutingService() {
    routingService.stop();
  }

  private void startUpDynamicRouting() {
    // create domain participant for administration interface
    domainParticipantAdministration = createRemoteAdministrationDomainParticipant(0);

    // create routing service administration
    routingServiceCommandHelper = new RoutingServiceCommandHelper(
        domainParticipantAdministration);

    // wait for routing service to be discovered
    log.info("Waiting for remote administration interface of routing service to be discovered");
    if (routingServiceCommandHelper.waitForRoutingService(ROUTING_SERVICE_NAME, 30, TimeUnit.SECONDS)) {
      log.info("Remote administration interface of routing service was discovered");
    } else {
      log.error("Remote administration interface of routing service could not be discovered within time out");
    }

    // create domain participant for discovery
    domainParticipantDiscovery = createDiscoveryDomainParticipant(0);

    // create dynamic partition commander
    dynamicPartitionCommander = new DynamicPartitionCommander(
        routingServiceCommandHelper,
        new DynamicPartitionCommanderProviderImpl("Default"),
        ROUTING_SERVICE_NAME
    );

    // create dynamic partition observer
    dynamicPartitionObserver = new DynamicPartitionObserver();

    // add filters to dynamic partition observer
    dynamicPartitionObserver.addFilter(new RtiTopicFilter());
    dynamicPartitionObserver.addFilter(new RoutingServiceEntitiesFilter());
    dynamicPartitionObserver.addFilter(new RoutingServiceGroupEntitiesFilter(ROUTING_SERVICE_NAME));
    dynamicPartitionObserver.addFilter(new WildcardPartitionFilter());

    // add listener to dynamic partition observer
    dynamicPartitionObserver.addListener(dynamicPartitionCommander);

    // create new publication observer
    publicationObserver = new PublicationObserver(domainParticipantDiscovery);
    publicationObserver.addListener(dynamicPartitionObserver, false);

    // create new subscription observer
    subscriptionObserver = new SubscriptionObserver(domainParticipantDiscovery);
    subscriptionObserver.addListener(dynamicPartitionObserver, false);

    // enable discovery domain participant
    domainParticipantDiscovery.enable();
  }

  private void shutdownDynamicRouting() {
    if (publicationObserver != null) {
      publicationObserver.close();
    }
    if (subscriptionObserver != null) {
      subscriptionObserver.close();
    }
    if (dynamicPartitionObserver != null) {
      dynamicPartitionObserver.close();
    }
    if (dynamicPartitionCommander != null) {
      dynamicPartitionCommander.close();
    }

    if (domainParticipantAdministration != null) {
      domainParticipantAdministration.delete_contained_entities();
      DomainParticipantFactory.get_instance().delete_participant(domainParticipantAdministration);
    }
    if (domainParticipantDiscovery != null) {
      domainParticipantDiscovery.delete_contained_entities();
      DomainParticipantFactory.get_instance().delete_participant(domainParticipantDiscovery);
    }
    routingServiceCommandHelper = null;
  }

  private DomainParticipant createRemoteAdministrationDomainParticipant(
      int domainId
  ) {
    return createDomainParticipant(domainId, "RTI Routing Service: remote administration");
  }

  private DomainParticipant createDiscoveryDomainParticipant(
      int domainId
  ) {
    // disable auto-enable -> THIS IS CRUCIAL TO WORK CORRECTLY
    AutoEnableCreatedEntitiesHelper.disable();

    // create discovery participant
    DomainParticipant domainParticipant = createDomainParticipant(domainId, "RTI Routing Service: discovery");

    // enable auto-enable
    AutoEnableCreatedEntitiesHelper.enable();

    return domainParticipant;
  }

  private DomainParticipant createDomainParticipant(
      int domainId,
      String participantName
  ) {
    // create default participant qos marked as routing service entity
    DomainParticipantQos domainParticipantQos = new DomainParticipantQos();
    DomainParticipantFactory.get_instance().get_default_participant_qos(domainParticipantQos);
    domainParticipantQos.service.kind = ServiceQosPolicyKind.ROUTING_SERVICE_QOS;
    domainParticipantQos.participant_name.name = participantName;

    // create domain participant for administration interface
    return DomainParticipantFactory.get_instance().create_participant(
        domainId,
        domainParticipantQos,
        null,
        StatusKind.STATUS_MASK_NONE
    );
  }
}
