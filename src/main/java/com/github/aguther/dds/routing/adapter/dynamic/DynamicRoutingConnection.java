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

package com.github.aguther.dds.routing.adapter.dynamic;

import com.github.aguther.dds.discovery.observer.PublicationObserver;
import com.github.aguther.dds.discovery.observer.SubscriptionObserver;
import com.github.aguther.dds.routing.adapter.empty.EmptySession;
import com.github.aguther.dds.routing.adapter.empty.EmptyStreamReader;
import com.github.aguther.dds.routing.adapter.empty.EmptyStreamWriter;
import com.github.aguther.dds.routing.dynamic.command.DynamicPartitionCommander;
import com.github.aguther.dds.routing.dynamic.observer.DynamicPartitionObserver;
import com.github.aguther.dds.routing.dynamic.observer.filter.RoutingServiceEntitiesFilter;
import com.github.aguther.dds.routing.dynamic.observer.filter.RoutingServiceGroupEntitiesFilter;
import com.github.aguther.dds.routing.dynamic.observer.filter.RtiTopicFilter;
import com.github.aguther.dds.util.AutoEnableCreatedEntitiesHelper;
import com.github.aguther.dds.routing.util.RoutingServiceCommandHelper;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.ServiceQosPolicyKind;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.routingservice.adapter.DiscoveryConnection;
import com.rti.routingservice.adapter.Session;
import com.rti.routingservice.adapter.StreamReader;
import com.rti.routingservice.adapter.StreamReaderListener;
import com.rti.routingservice.adapter.StreamWriter;
import com.rti.routingservice.adapter.infrastructure.AdapterException;
import com.rti.routingservice.adapter.infrastructure.StreamInfo;
import java.io.Closeable;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicRoutingConnection implements DiscoveryConnection, Closeable {

  private static final Logger log;

  private static final String PROPERTY_ADMINISTRATION_DOMAIN_ID;

  private static final String PROPERTY_ADMINISTRATION_DISCOVERY_WAIT_TIME;
  private static final String DEFAULT_PROPERTY_ADMINISTRATION_DISCOVERY_WAIT_TIME;

  private static final String PROPERTY_ADMINISTRATION_REQUEST_TIMEOUT;
  private static final String DEFAULT_PROPERTY_ADMINISTRATION_REQUEST_TIMEOUT;

  private static final String PROPERTY_ADMINISTRATION_REQUEST_RETRY_DELAY;
  private static final String DEFAULT_PROPERTY_ADMINISTRATION_REQUEST_RETRY_DELAY;

  private static final String PROPERTY_DISCOVERY_DOMAIN_ID;

  static {
    log = LoggerFactory.getLogger(DynamicRoutingAdapter.class);

    PROPERTY_ADMINISTRATION_DOMAIN_ID = "dynamic_routing_adapter.administration.domain_id";

    PROPERTY_ADMINISTRATION_DISCOVERY_WAIT_TIME = "dynamic_routing_adapter.administration.discovery.wait_time";
    DEFAULT_PROPERTY_ADMINISTRATION_DISCOVERY_WAIT_TIME = "30000";

    PROPERTY_ADMINISTRATION_REQUEST_TIMEOUT = "dynamic_routing_adapter.administration.request.timeout";
    DEFAULT_PROPERTY_ADMINISTRATION_REQUEST_TIMEOUT = "15000";

    PROPERTY_ADMINISTRATION_REQUEST_RETRY_DELAY = "dynamic_routing_adapter.administration.request.retry_delay";
    DEFAULT_PROPERTY_ADMINISTRATION_REQUEST_RETRY_DELAY = "45000";

    PROPERTY_DISCOVERY_DOMAIN_ID = "dynamic_routing_adapter.discovery.domain_id";
  }

  private DomainParticipant domainParticipantAdministration;
  private DomainParticipant domainParticipantDiscovery;
  private RoutingServiceCommandHelper routingServiceCommandHelper;
  private DynamicPartitionObserver dynamicPartitionObserver;
  private PublicationObserver publicationObserver;
  private SubscriptionObserver subscriptionObserver;

  private DynamicPartitionCommander dynamicPartitionCommander;

  DynamicRoutingConnection(
      String routingServiceName,
      String routingServiceGroupName,
      Properties properties
  ) {
    log.info("Creating connection");

    if (log.isDebugEnabled()) {
      for (String key : properties.stringPropertyNames()) {
        log.info(
            "key='{}', value='{}'",
            key,
            properties.getProperty(key)
        );
      }
    }

    // create domain participant for administration interface
    domainParticipantAdministration = createRemoteAdministrationDomainParticipant(
        Integer.parseInt(properties.getProperty(PROPERTY_ADMINISTRATION_DOMAIN_ID)));

    // create routing service administration
    routingServiceCommandHelper = new RoutingServiceCommandHelper(
        domainParticipantAdministration);

    // wait for routing service to be discovered
    log.info("Waiting for remote administration interface of routing service to be discovered");
    if (routingServiceCommandHelper.waitForRoutingService(
        routingServiceName,
        Long.parseLong(properties.getProperty(
            PROPERTY_ADMINISTRATION_DISCOVERY_WAIT_TIME,
            DEFAULT_PROPERTY_ADMINISTRATION_DISCOVERY_WAIT_TIME
        )),
        TimeUnit.MILLISECONDS)) {
      log.info("Remote administration interface of routing service was discovered");
    } else {
      log.warn("Remote administration interface of routing service could not be discovered within time out");
    }

    // create domain participant for discovery
    domainParticipantDiscovery = createDiscoveryDomainParticipant(
        Integer.parseInt(properties.getProperty(PROPERTY_DISCOVERY_DOMAIN_ID)));

    // create configuration filter
    ConfigurationFilter configurationFilter = new ConfigurationFilter(properties);

    // create dynamic partition observer
    dynamicPartitionObserver = new DynamicPartitionObserver();
    // filter out RTI topics
    dynamicPartitionObserver.addFilter(new RtiTopicFilter());
    // filter out routing service entities
    dynamicPartitionObserver.addFilter(new RoutingServiceEntitiesFilter());
    // filter out entities belonging to the same routing service group
    dynamicPartitionObserver.addFilter(new RoutingServiceGroupEntitiesFilter(routingServiceGroupName));
    dynamicPartitionObserver.addFilter(configurationFilter);

    // create commander
    dynamicPartitionCommander = new DynamicPartitionCommander(
        routingServiceCommandHelper,
        configurationFilter,
        routingServiceName,
        Long.parseLong(properties.getProperty(
            PROPERTY_ADMINISTRATION_REQUEST_RETRY_DELAY,
            DEFAULT_PROPERTY_ADMINISTRATION_REQUEST_RETRY_DELAY
        )),
        TimeUnit.MILLISECONDS,
        Long.parseLong(properties.getProperty(
            PROPERTY_ADMINISTRATION_REQUEST_TIMEOUT,
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

    log.info("Connection created");
  }

  @Override
  public void close() {
    log.info("Closing connection");

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

    log.info("Connection closed");
  }

  @Override
  public Session createSession(
      Properties properties
  ) throws AdapterException {
    return new EmptySession();
  }

  @Override
  public void deleteSession(
      Session session
  ) throws AdapterException {
    // do nothing
  }

  @Override
  public StreamReader createStreamReader(
      Session session,
      StreamInfo streamInfo,
      Properties properties,
      StreamReaderListener streamReaderListener
  ) throws AdapterException {
    return new EmptyStreamReader();
  }

  @Override
  public void deleteStreamReader(
      StreamReader streamReader
  ) throws AdapterException {
    // do nothing
  }

  @Override
  public StreamWriter createStreamWriter(
      Session session,
      StreamInfo streamInfo,
      Properties properties
  ) throws AdapterException {
    return new EmptyStreamWriter();
  }

  @Override
  public void deleteStreamWriter(
      StreamWriter streamWriter
  ) throws AdapterException {
    // do nothing
  }

  @Override
  public Properties getAttributes() throws AdapterException {
    throw new AdapterException(0, "Operation not supported");
  }

  @Override
  public void update(
      Properties properties
  ) throws AdapterException {
    // do nothing
  }

  @Override
  public StreamReader getInputStreamDiscoveryReader() throws AdapterException {
    return new EmptyStreamReader();
  }

  @Override
  public StreamReader getOutputStreamDiscoveryReader() throws AdapterException {
    return new EmptyStreamReader();
  }

  @Override
  public Object copyTypeRepresentation(
      Object o
  ) throws AdapterException {
    throw new AdapterException(0, "Operation not supported");
  }

  @Override
  public void deleteTypeRepresentation(
      Object o
  ) throws AdapterException {
    // do nothing
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
