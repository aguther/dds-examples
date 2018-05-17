/*
 * MIT License
 *
 * Copyright (c) 2018 Andreas Guther
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
import com.github.aguther.dds.routing.dynamic.command.remote.DynamicPartitionCommander;
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
import com.rti.routingservice.RoutingService;
import java.io.Closeable;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.commons.text.StringSubstitutor;
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

  private static final String PROPERTY_ADMINISTRATION_LOCAL
      = "administration.local";
  private static final String DEFAULT_PROPERTY_ADMINISTRATION_LOCAL
      = "false";

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

  private final String propertiesPrefix;
  private final Properties properties;

  private DomainParticipant domainParticipantAdministration;
  private final DomainParticipant domainParticipantDiscovery;

  private final PublicationObserver publicationObserver;
  private final SubscriptionObserver subscriptionObserver;

  private final DynamicPartitionObserver dynamicPartitionObserver;

  private DynamicPartitionCommander dynamicPartitionCommanderRemote;
  private RoutingServiceCommandInterface routingServiceCommandInterface;

  /**
   * Instantiates a new dynamic routing.
   *
   * @param routingServiceName name of the routing service to command
   * @param routingServiceGroupName group of the routing service to command
   * @param propertiesPrefix prefix to be used for properties
   * @param properties configuration properties
   */
  public DynamicRoutingManager(
      final String routingServiceName,
      final String routingServiceGroupName,
      final String propertiesPrefix,
      final Properties properties
  ) {
    this(
        null,
        routingServiceName,
        routingServiceGroupName,
        propertiesPrefix,
        properties
    );
  }

  /**
   * Instantiates a new dynamic routing.
   *
   * @param routingService routing service library object (if local interface is used, otherwise provide 'null')
   * @param routingServiceName name of the routing service to command
   * @param routingServiceGroupName group of the routing service to command
   * @param propertiesPrefix prefix to be used for properties
   * @param properties configuration properties
   */
  public DynamicRoutingManager(
      final RoutingService routingService,
      final String routingServiceName,
      final String routingServiceGroupName,
      final String propertiesPrefix,
      final Properties properties
  ) {
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

    // store properties and prefix
    this.propertiesPrefix = propertiesPrefix;
    this.properties = properties;

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

    // create domain participant for discovery
    domainParticipantDiscovery = createDiscoveryDomainParticipant(
        Integer.parseInt(StringSubstitutor.replace(getProperty(PROPERTY_DISCOVERY_DOMAIN_ID), System.getenv()))
    );

    // create new publication observer
    publicationObserver = new PublicationObserver(domainParticipantDiscovery);
    publicationObserver.addListener(dynamicPartitionObserver, false);

    // create new subscription observer
    subscriptionObserver = new SubscriptionObserver(domainParticipantDiscovery);
    subscriptionObserver.addListener(dynamicPartitionObserver, false);

    // depending on provided property start either local or remote administration interface
    if (Boolean.parseBoolean(StringSubstitutor
        .replace(getProperty(PROPERTY_ADMINISTRATION_LOCAL, DEFAULT_PROPERTY_ADMINISTRATION_LOCAL), System.getenv()))) {
      createLocalAdministration(routingService, configurationFilterProvider);
    } else {
      createRemoteAdministration(routingServiceName, configurationFilterProvider);
    }

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
    if (dynamicPartitionCommanderRemote != null) {
      dynamicPartitionCommanderRemote.close();
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
   * Returns the current properties.
   *
   * @return current properties
   */
  public Properties getProperties() {
    return properties;
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
   * Gets a property using the correct prefix.
   *
   * @param name name of the property
   * @return property value
   */
  private String getProperty(
      String name
  ) {
    return properties.getProperty(
        String.format("%s%s", propertiesPrefix, name)
    );
  }

  /**
   * Gets a property using the correct prefix.
   *
   * @param name name of the property
   * @param defaultValue default value to provide if property not found
   * @return property value, provided default if not found
   */
  private String getProperty(
      String name,
      String defaultValue
  ) {
    return properties.getProperty(
        String.format("%s%s", propertiesPrefix, name),
        defaultValue
    );
  }

  /**
   * Creates the local administration interface (using routing service library)
   *
   * @param routingService routing service to administrate
   * @param configurationFilterProvider configuration filter provider
   */
  private void createLocalAdministration(
      RoutingService routingService,
      ConfigurationFilterProvider configurationFilterProvider
  ) {
    checkNotNull(
        routingService,
        "Routing Service must not be null when local interface enabled"
    );
    checkNotNull(
        configurationFilterProvider,
        "Configuration Filter Provider must not be null"
    );

    // add listener to dynamic partition observer
    dynamicPartitionObserver.addListener(
        new com.github.aguther.dds.routing.dynamic.command.local.DynamicPartitionCommander(
            routingService,
            configurationFilterProvider
        )
    );
  }

  /**
   * Creates the remote administration interface.
   *
   * @param routingServiceName routing service name to administrate
   * @param configurationFilterProvider configuration filter provider
   */
  private void createRemoteAdministration(
      String routingServiceName,
      ConfigurationFilterProvider configurationFilterProvider
  ) {
    checkArgument(
        !Strings.isNullOrEmpty(routingServiceName),
        "Routing Service name is expected not to be null or empty"
    );
    checkNotNull(
        configurationFilterProvider,
        "Configuration Filter Provider must not be null"
    );

    // create domain participant for administration interface and ensure it will be enabled
    domainParticipantAdministration = createRemoteAdministrationDomainParticipant(
        Integer.parseInt(StringSubstitutor.replace(getProperty(PROPERTY_ADMINISTRATION_DOMAIN_ID), System.getenv()))
    );
    domainParticipantAdministration.enable();

    // create routing service administration
    routingServiceCommandInterface = new RoutingServiceCommandInterface(
        domainParticipantAdministration);

    // wait for routing service to be discovered
    LOGGER.info("Waiting for remote administration interface of routing service to be discovered");
    if (routingServiceCommandInterface.waitForDiscovery(
        routingServiceName,
        Long.parseLong(StringSubstitutor.replace(
            getProperty(
                PROPERTY_ADMINISTRATION_DISCOVERY_WAIT_TIME,
                DEFAULT_PROPERTY_ADMINISTRATION_DISCOVERY_WAIT_TIME
            ),
            System.getenv()
        )),
        TimeUnit.MILLISECONDS)) {
      LOGGER.info("Remote administration interface of routing service was discovered");
    } else {
      LOGGER.warn("Remote administration interface of routing service could not be discovered within time out");
    }

    // create commander
    dynamicPartitionCommanderRemote = new DynamicPartitionCommander(
        routingServiceCommandInterface,
        configurationFilterProvider,
        routingServiceName,
        Long.parseLong(StringSubstitutor.replace(
            getProperty(
                PROPERTY_ADMINISTRATION_REQUEST_RETRY_DELAY,
                DEFAULT_PROPERTY_ADMINISTRATION_REQUEST_RETRY_DELAY
            ),
            System.getenv()
        )),
        TimeUnit.MILLISECONDS,
        Long.parseLong(StringSubstitutor.replace(
            getProperty(
                PROPERTY_ADMINISTRATION_REQUEST_TIMEOUT,
                DEFAULT_PROPERTY_ADMINISTRATION_REQUEST_TIMEOUT
            ),
            System.getenv()
        )),
        TimeUnit.MILLISECONDS
    );

    // add listener to dynamic partition observer
    dynamicPartitionObserver.addListener(dynamicPartitionCommanderRemote);
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
    // get current state
    boolean wasEnabled = AutoEnableCreatedEntitiesHelper.isEnabled();

    // disable auto-enable -> THIS IS CRUCIAL TO WORK CORRECTLY
    AutoEnableCreatedEntitiesHelper.disable();

    // create discovery participant
    DomainParticipant domainParticipant = createDomainParticipant(domainId, "RTI Routing Service: discovery");

    // enable auto-enable if it was enabled previously
    if (wasEnabled) {
      AutoEnableCreatedEntitiesHelper.enable();
    }

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
