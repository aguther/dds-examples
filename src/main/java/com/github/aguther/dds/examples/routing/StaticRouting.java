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

import com.github.aguther.dds.util.Slf4jDdsLogger;
import com.rti.routingservice.RoutingService;
import com.rti.routingservice.RoutingServiceProperty;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticRouting {

  private static final Logger log;

  private static boolean shouldTerminate;

  static {
    log = LoggerFactory.getLogger(StaticRouting.class);
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

    // setup routing service properties
    final RoutingServiceProperty routingServiceProperty = new RoutingServiceProperty();
    routingServiceProperty.cfgFile = "routing-static.xml";
    routingServiceProperty.serviceName = "dds-examples-routing-static";
    routingServiceProperty.applicationName = routingServiceProperty.serviceName;
    routingServiceProperty.serviceVerbosity = 3;

    // create routing service instance
    try (RoutingService routingService = new RoutingService(routingServiceProperty)) {

      // start routing service
      routingService.start();

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
