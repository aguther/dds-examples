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

package com.github.aguther.dds.routing.dynamic;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.github.aguther.dds.discovery.observer.PublicationObserver;
import com.github.aguther.dds.discovery.observer.SubscriptionObserver;
import com.github.aguther.dds.routing.dynamic.command.DynamicPartitionCommander;
import com.github.aguther.dds.routing.dynamic.observer.DynamicPartitionObserver;
import com.github.aguther.dds.routing.dynamic.observer.filter.RoutingServiceGroupEntitiesFilter;
import com.github.aguther.dds.routing.dynamic.observer.filter.RtiTopicFilter;
import com.github.aguther.dds.routing.util.RoutingServiceCommandInterface;
import com.github.aguther.dds.util.AutoEnableCreatedEntitiesHelper;
import com.google.common.base.Strings;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.ServiceQosPolicyKind;
import com.rti.dds.infrastructure.StatusKind;
import java.io.Closeable;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a connection to provide a function to dynamically route topics based on their partition
 * without loosing their origin (this happens when using asterisk or multiple partitions).
 *
 * This function is realized by creating a domain participant for discovery and remote administration of the target
 * routing service. Whenever a topic is discovered and a appropriate configuration is found, a session and route is
 * created accordingly. The same applies vice versa on loosing discovery.
 */
public class DynamicRoutingManager implements Closeable {

  private static final String PROPERTY_ADMINISTRATION_DOMAIN_ID
      = "administration.domain_id";

  private static final String PROPERTY_ADMINISTRATION_DISCOVERY_WAIT_TIME
      = "administration.discovery.wait_time";
  private static final String DEFAULT_PROPERTY_ADMINISTRATION_DISCOVERY_WAIT_TIME
      = "30000";

  private static final String PROPERTY_ADMINISTRATION_REQUEST_TIMEOUT
      = "administration.request.timeout";
  private static final String DEFAULT_PROPERTY_ADMINISTRATION_REQUEST_TIMEOUT
      = "15000";

  private static final String PROPERTY_ADMINISTRATION_REQUEST_RETRY_DELAY
      = "administration.request.retry_delay";
  private static final String DEFAULT_PROPERTY_ADMINISTRATION_REQUEST_RETRY_DELAY
      = "45000";

  private static final String PROPERTY_DISCOVERY_DOMAIN_ID
      = "discovery.domain_id";

  private static final Logger LOGGER = LoggerFactory.getLogger(DynamicRoutingManager.class);

  private final DomainParticipant domainParticipantAdministration;
  private final DomainParticipant domainParticipantDiscovery;

  private final PublicationObserver publicationObserver;
  private final SubscriptionObserver subscriptionObserver;

  private final DynamicPartitionObserver dynamicPartitionObserver;

  private final DynamicPartitionCommander dynamicPartitionCommander;
  private final RoutingServiceCommandInterface routingServiceCommandInterface;

  /**
   * Instantiates a new dynamic routing.
   *
   * @param routingServiceName name of the routing service to command
   * @param routingServiceGroupName group of the routing service to command
   * @param properties configuration properties
   */
  public DynamicRoutingManager(
      final String routingServiceName,
      final String routingServiceGroupName,
      final String propertiesPrefix,
      final Properties properties
  ) {
    checkArgument(
        !Strings.isNullOrEmpty(routingServiceName),
        "Routing Service name is expected not to be null or empty"
    );
    checkNotNull(
        routingServiceGroupName,
        "Group name must not be null"
    );
    checkNotNull(
        propertiesPrefix,
        "Property prefix must not be null"
    );
    checkNotNull(
        properties,
        "Properties must not be null"
    );

    // log properties when info is enabled
    if (LOGGER.isInfoEnabled()) {
      for (String key : properties.stringPropertyNames()) {
        LOGGER.info(
            "key='{}', value='{}'",
            key,
            properties.getProperty(key)
        );
      }
    }

    // create domain participant for administration interface
    domainParticipantAdministration = createRemoteAdministrationDomainParticipant(
        Integer.parseInt(properties.getProperty(String.format(
            "%s%s", propertiesPrefix, PROPERTY_ADMINISTRATION_DOMAIN_ID))));

    // create routing service administration
    routingServiceCommandInterface = new RoutingServiceCommandInterface(
        domainParticipantAdministration);

    // wait for routing service to be discovered
    LOGGER.info("Waiting for remote administration interface of routing service to be discovered");
    if (routingServiceCommandInterface.waitForDiscovery(
        routingServiceName,
        Long.parseLong(properties.getProperty(
            String.format("%s%s", propertiesPrefix, PROPERTY_ADMINISTRATION_DISCOVERY_WAIT_TIME),
            DEFAULT_PROPERTY_ADMINISTRATION_DISCOVERY_WAIT_TIME
        )),
        TimeUnit.MILLISECONDS)) {
      LOGGER.info("Remote administration interface of routing service was discovered");
    } else {
      LOGGER.warn("Remote administration interface of routing service could not be discovered within time out");
    }

    // create domain participant for discovery
    domainParticipantDiscovery = createDiscoveryDomainParticipant(
        Integer.parseInt(properties.getProperty(
            String.format("%s%s", propertiesPrefix, PROPERTY_DISCOVERY_DOMAIN_ID))));

    // create configuration filter
    ConfigurationFilterProvider configurationFilterProvider = new ConfigurationFilterProvider(
        propertiesPrefix, properties);

    // create dynamic partition observer
    dynamicPartitionObserver = new DynamicPartitionObserver();
    // filter out RTI topics
    dynamicPartitionObserver.addFilter(new RtiTopicFilter());
    // filter out entities belonging to the same routing service group
    dynamicPartitionObserver.addFilter(new RoutingServiceGroupEntitiesFilter(routingServiceGroupName));
    // filter out entities that have no configuration
    dynamicPartitionObserver.addFilter(configurationFilterProvider);

    // create commander
    dynamicPartitionCommander = new DynamicPartitionCommander(
        routingServiceCommandInterface,
        configurationFilterProvider,
        routingServiceName,
        Long.parseLong(properties.getProperty(
            String.format("%s%s", propertiesPrefix, PROPERTY_ADMINISTRATION_REQUEST_RETRY_DELAY),
            DEFAULT_PROPERTY_ADMINISTRATION_REQUEST_RETRY_DELAY
        )),
        TimeUnit.MILLISECONDS,
        Long.parseLong(properties.getProperty(
            String.format("%s%s", propertiesPrefix, PROPERTY_ADMINISTRATION_REQUEST_TIMEOUT),
            DEFAULT_PROPERTY_ADMINISTRATION_REQUEST_TIMEOUT
        )),
        TimeUnit.MILLISECONDS
    );
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

  @Override
  public void close() {
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
    if (routingServiceCommandInterface != null) {
      routingServiceCommandInterface.close();
    }

    if (domainParticipantAdministration != null) {
      domainParticipantAdministration.delete_contained_entities();
      DomainParticipantFactory.get_instance().delete_participant(domainParticipantAdministration);
    }
    if (domainParticipantDiscovery != null) {
      domainParticipantDiscovery.delete_contained_entities();
      DomainParticipantFactory.get_instance().delete_participant(domainParticipantDiscovery);
    }
  }

  /**
   * Returns the current properties (currently not supported).
   *
   * @return current properties
   */
  public Properties getProperties() {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  /**
   * Updates the properties (not yet implemented).
   *
   * @param properties new configuration properties
   */
  public void update(
      final Properties properties
  ) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  /**
   * Creates a domain participant for remote administration (will be auto-enabled).
   *
   * @param domainId domain id
   * @return enabled domain participant if successful, otherwise null
   */
  private DomainParticipant createRemoteAdministrationDomainParticipant(
      final int domainId
  ) {
    return createDomainParticipant(domainId, "RTI Routing Service: remote administration");
  }

  /**
   * Creates a domain participant for discovery (will NOT be enabled).
   *
   * @param domainId domain id
   * @return not enabled domain participant if successful, otherwise null
   */
  private DomainParticipant createDiscoveryDomainParticipant(
      final int domainId
  ) {
    // disable auto-enable -> THIS IS CRUCIAL TO WORK CORRECTLY
    AutoEnableCreatedEntitiesHelper.disable();

    // create discovery participant
    DomainParticipant domainParticipant = createDomainParticipant(domainId, "RTI Routing Service: discovery");

    // enable auto-enable
    AutoEnableCreatedEntitiesHelper.enable();

    return domainParticipant;
  }

  /**
   * Creates a domain participant marked as routing service and with correct participant name.
   *
   * @param domainId domain id
   * @param participantName participant name
   * @return domain participant if successful, otherwise null
   */
  private DomainParticipant createDomainParticipant(
      final int domainId,
      final String participantName
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
