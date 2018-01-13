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

package com.github.aguther.dds.examples.routing;

import com.github.aguther.dds.logging.Slf4jDdsLogger;
import com.github.aguther.dds.routing.dynamic.DynamicRoutingManager;
import com.google.common.util.concurrent.AbstractIdleService;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantFactoryQos;
import com.rti.routingservice.RoutingService;
import com.rti.routingservice.RoutingServiceProperty;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicRouting extends AbstractIdleService {

  private static final String ROUTING_SERVICE_NAME = "dds-examples-routing-dynamic";
  private static final String ROUTING_SERVICE_CONFIG_FILE = "routing-dynamic.xml";

  private static final Logger LOGGER = LoggerFactory.getLogger(DynamicRouting.class);

  private static DynamicRouting serviceInstance;

  private RoutingService routingService;
  private DynamicRoutingManager dynamicRoutingManager;

  public static void main(
      final String[] args
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
    LOGGER.info("Service terminated");
  }

  private static void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      LOGGER.info("Shutdown signal received");
      if (serviceInstance != null) {
        serviceInstance.stopAsync();
        serviceInstance.awaitTerminated();
      }
      LOGGER.info("Shutdown signal finished");
    }));
  }

  @Override
  protected void startUp() throws Exception {
    // log service start
    LOGGER.info("Service is starting");

    // prepare DDS factory
    startUpPrepareDomainParticipantFactory();

    // start DDS logger
    startUpDdsLogger();

    // start routing service
    startUpRoutingService();

    // start dynamic routing
    startUpDynamicRouting();

    // log service start
    LOGGER.info("Service start finished");
  }

  @Override
  protected void shutDown() throws Exception {
    // log service start
    LOGGER.info("Service is shutting down");

    // shutdown dynamic routing
    shutdownDynamicRouting();

    // shutdown routing service
    shutdownRoutingService();

    // log service start
    LOGGER.info("Service shutdown finished");
  }

  private void startUpPrepareDomainParticipantFactory() {
    DomainParticipantFactoryQos domainParticipantFactoryQos = new DomainParticipantFactoryQos();
    DomainParticipantFactory.get_instance().get_qos(domainParticipantFactoryQos);
    domainParticipantFactoryQos.resource_limits.max_objects_per_thread = 4096;
    DomainParticipantFactory.get_instance().set_qos(domainParticipantFactoryQos);
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

  private void startUpDynamicRouting() throws IOException {
    // load properties
    final Properties properties = new Properties();
    try (InputStream stream = getClass().getResourceAsStream("/dynamic_routing.properties")) {
      properties.load(stream);
    }

    // start dynamic routing
    dynamicRoutingManager = new DynamicRoutingManager(
        ROUTING_SERVICE_NAME,
        ROUTING_SERVICE_NAME,
        "",
        properties
    );
  }

  private void shutdownDynamicRouting() {
    if (dynamicRoutingManager != null) {
      dynamicRoutingManager.close();
    }
  }
}
