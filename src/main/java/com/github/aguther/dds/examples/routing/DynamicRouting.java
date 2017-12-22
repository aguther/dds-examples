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
import com.github.aguther.dds.util.Slf4jDdsLogger;
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

public class DynamicRouting {

  private static final String ROUTING_SERVICE_NAME;

  private static final Logger log;

  private static boolean shouldTerminate;

  static {
    ROUTING_SERVICE_NAME = "dds-examples-routing-dynamic";

    log = LoggerFactory.getLogger(DynamicRouting.class);
  }

  public static void main(String[] args) throws InterruptedException {

    // register shutdown hook
    registerShutdownHook();

    // register logger DDS messages
    try {
      Slf4jDdsLogger.createRegisterLogger();
    } catch (IOException e) {
      log.error("Failed to create and register DDS logging device.", e);
      return;
    }

    log.info("Starting routing service");

    // setup routing service properties
    final RoutingServiceProperty routingServiceProperty = new RoutingServiceProperty();
    routingServiceProperty.cfgFile = "routing.xml";
    routingServiceProperty.serviceName = ROUTING_SERVICE_NAME;
    routingServiceProperty.applicationName = routingServiceProperty.serviceName;
    routingServiceProperty.serviceVerbosity = 3;

    // create routing service instance
    try (RoutingService routingService = new RoutingService(routingServiceProperty)) {

      // start routing service
      routingService.start();
      log.info("Routing service was started");

      // start dynamic routing
      startupDynamicRouting();

      while (!shouldTerminate) {
        Thread.sleep(1000);
      }

      // stop routing service
      routingService.stop();
    }
  }

  private static void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Shutdown signal received...");
      shouldTerminate = true;
    }));
  }

  private static void startupDynamicRouting() {
    log.info("Starting dynamic routing");

    // create domain participant for administration interface
    DomainParticipant domainParticipantAdministration = createRemoteAdministrationDomainParticipant(0);

    // create routing service administration
    RoutingServiceCommandHelper routingServiceCommandHelper = new RoutingServiceCommandHelper(
        domainParticipantAdministration);

    // wait for routing service to be discovered
    log.info("Waiting for remote administration interface of routing service to be discovered");
    if (routingServiceCommandHelper.waitForRoutingService(ROUTING_SERVICE_NAME, 30, TimeUnit.SECONDS)) {
      log.info("Remote administration interface of routing service was discovered");
    } else {
      log.error("Remote administration interface of routing service could not be discovered within time out");
    }

    // create domain participant for discovery
    DomainParticipant domainParticipantDiscovery = createDiscoveryDomainParticipant(0);

    // create dynamic partition observer
    DynamicPartitionObserver dynamicPartitionObserver = new DynamicPartitionObserver(domainParticipantDiscovery);
    // add filters to dynamic partition observer
    dynamicPartitionObserver.addFilter(new RtiTopicFilter());
    dynamicPartitionObserver.addFilter(new RoutingServiceEntitiesFilter());
    dynamicPartitionObserver.addFilter(new RoutingServiceGroupEntitiesFilter(ROUTING_SERVICE_NAME));
    dynamicPartitionObserver.addFilter(new WildcardPartitionFilter());
    // add listener to dynamic partition observer
    dynamicPartitionObserver.addListener(
        new DynamicPartitionCommander(
            routingServiceCommandHelper,
            new DynamicPartitionCommanderProviderImpl("Default"),
            ROUTING_SERVICE_NAME
        )
    );

    // create new publication observer
    PublicationObserver publicationObserver = new PublicationObserver(domainParticipantDiscovery);
    publicationObserver.addListener(dynamicPartitionObserver, false);

    // create new subscription observer
    SubscriptionObserver subscriptionObserver = new SubscriptionObserver(domainParticipantDiscovery);
    subscriptionObserver.addListener(dynamicPartitionObserver, false);

    // enable discovery domain participant
    domainParticipantDiscovery.enable();

    log.info("Dynamic routing was started");
  }

  private static DomainParticipant createRemoteAdministrationDomainParticipant(
      int domainId
  ) {
    return createDomainParticipant(domainId, "RTI Routing Service: remote administration");
  }

  private static DomainParticipant createDiscoveryDomainParticipant(
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

  private static DomainParticipant createDomainParticipant(
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
