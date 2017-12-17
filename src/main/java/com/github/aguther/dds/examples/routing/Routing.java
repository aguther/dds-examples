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

import com.github.aguther.dds.examples.discovery.PublicationObserver;
import com.github.aguther.dds.examples.discovery.PublicationObserverListener;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantFactoryQos;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.PartitionQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicDataTypeSupport;
import com.rti.routingservice.RoutingService;
import com.rti.routingservice.RoutingServiceProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Routing {

  private static final Logger log;

  private static boolean shouldTerminate;

  static {
    log = LoggerFactory.getLogger(Routing.class);
  }

  public static void main(String[] args) throws InterruptedException {

    // register shutdown hook
    registerShutdownHook();

    // setup routing service properties
    final RoutingServiceProperty routingServiceProperty = new RoutingServiceProperty();
    routingServiceProperty.cfgFile = "routing.xml";
    routingServiceProperty.serviceName = "dds-examples-routing";
    routingServiceProperty.applicationName = routingServiceProperty.serviceName;

    // create routing service instance
    try (RoutingService routingService = new RoutingService(routingServiceProperty)) {

      // start routing service
      routingService.start();

      // disable auto enable
      DomainParticipantFactoryQos domainParticipantFactoryQos = new DomainParticipantFactoryQos();
      DomainParticipantFactory.get_instance().get_qos(domainParticipantFactoryQos);
      domainParticipantFactoryQos.entity_factory.autoenable_created_entities = false;
      DomainParticipantFactory.get_instance().set_qos(domainParticipantFactoryQos);

      // create another participant
      DomainParticipantQos domainParticipantQos = new DomainParticipantQos();
      DomainParticipant domainParticipant = DomainParticipantFactory.get_instance().create_participant(
          0,
          DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
          null,
          StatusKind.STATUS_MASK_NONE
      );
      domainParticipant.get_qos(domainParticipantQos);
      domainParticipantQos.participant_name.name = "Discovery";
      domainParticipant.set_qos(domainParticipantQos);

      // create new publication observer
      PublicationObserver publicationObserver = new PublicationObserver(domainParticipant);
      publicationObserver.addListener(new PublicationObserverListener() {
        @Override
        public void publicationDiscovered(
            InstanceHandle_t instanceHandle,
            PublicationBuiltinTopicData data
        ) {
          log.info(
              "Discovered Publication : instance='{}', topic='{}', type='{}'",
              instanceHandle.toString(),
              data.topic_name,
              data.type_name
          );
        }

        @Override
        public void publicationLost(
            InstanceHandle_t instanceHandle
        ) {
          log.info(
              "Lost Publication       : instance='{}'",
              instanceHandle.toString()
          );
        }
      });

      // enable participant
      domainParticipant.enable();

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
}
