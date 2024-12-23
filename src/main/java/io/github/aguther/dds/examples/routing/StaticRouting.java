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

package io.github.aguther.dds.examples.routing;

import com.google.common.util.concurrent.AbstractIdleService;
import com.rti.routingservice.RoutingService;
import com.rti.routingservice.RoutingServiceProperty;
import io.github.aguther.dds.logging.Slf4jDdsLogger;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StaticRouting extends AbstractIdleService {

  private static final String ROUTING_SERVICE_NAME = "dds-examples-routing-static";
  private static final String ROUTING_SERVICE_CONFIG_FILE = "configuration/routing-static.xml";
  private static final Logger LOGGER = LogManager.getLogger(StaticRouting.class);

  private static StaticRouting serviceInstance;

  private RoutingService routingService;

  public static void main(
    final String[] args
  ) {
    // register shutdown hook
    registerShutdownHook();

    // create service
    serviceInstance = new StaticRouting();

    // start the service
    serviceInstance.startAsync();

    // wait for termination
    serviceInstance.awaitTerminated();

    // service terminated
    LOGGER.info("Service terminated");
  }

  private static void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(
      () -> {
        LOGGER.info("Shutdown signal received");
        if (serviceInstance != null) {
          serviceInstance.stopAsync();
          serviceInstance.awaitTerminated();
        }
        LOGGER.info("Shutdown signal finished");
      },
      String.format("ShutdownHook-%s", StaticRouting.class.getName())
    ));
  }

  @Override
  protected void startUp() throws Exception {
    // log service start
    LOGGER.info("Service is starting");

    // create routing service
    startUpCreateRoutingService();

    // attach DDS logger
    startUpDdsLogger();

    // start routing service
    startUpStartRoutingService();

    // log service start
    LOGGER.info("Service start finished");
  }

  @Override
  protected void shutDown() throws Exception {
    // log service start
    LOGGER.info("Service is shutting down");

    // shutdown routing service
    shutdownRoutingService();

    // log service start
    LOGGER.info("Service shutdown finished");
  }

  private void startUpDdsLogger() throws IOException {
    Slf4jDdsLogger.createRegisterLogger();
  }

  private void startUpCreateRoutingService() {
    // setup routing service properties
    final RoutingServiceProperty routingServiceProperty = new RoutingServiceProperty();
    routingServiceProperty.cfgFile = ROUTING_SERVICE_CONFIG_FILE;
    routingServiceProperty.serviceName = ROUTING_SERVICE_NAME;
    routingServiceProperty.applicationName = routingServiceProperty.serviceName;
    routingServiceProperty.serviceVerbosity = 3;
    routingServiceProperty.enforceXsdValidation = false;

    // create routing service instance
    routingService = new RoutingService(routingServiceProperty);
  }

  private void startUpStartRoutingService() {
    // start routing service
    routingService.start();
  }

  private void shutdownRoutingService() {
    routingService.stop();
  }
}
