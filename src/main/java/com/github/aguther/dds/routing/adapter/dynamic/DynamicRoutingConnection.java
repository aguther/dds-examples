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

package com.github.aguther.dds.routing.adapter.dynamic;

import com.github.aguther.dds.routing.adapter.empty.EmptyConnection;
import com.github.aguther.dds.routing.dynamic.DynamicRoutingManager;
import com.rti.routingservice.adapter.infrastructure.AdapterException;
import java.io.Closeable;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class implements a connection to provide a function to dynamically route topics based on their partition
 * without loosing their origin (this happens when using asterisk or multiple partitions).
 *
 * This function is realized by creating a domain participant for discovery and remote administration of the target
 * routing service. Whenever a topic is discovered and a appropriate configuration is found, a session and route is
 * created accordingly. The same applies vice versa on loosing discovery.
 */
public class DynamicRoutingConnection extends EmptyConnection implements Closeable {

  private static final Logger LOGGER = LogManager.getLogger(DynamicRoutingConnection.class);

  private DynamicRoutingManager dynamicRoutingManager;

  DynamicRoutingConnection(
    final String routingServiceName,
    final String routingServiceGroupName,
    final Properties properties
  ) {
    LOGGER.info("Creating connection");

    if (LOGGER.isDebugEnabled()) {
      for (String key : properties.stringPropertyNames()) {
        LOGGER.debug(
          "key='{}', value='{}'",
          key,
          properties.getProperty(key)
        );
      }
    }

    dynamicRoutingManager = new DynamicRoutingManager(
      routingServiceName,
      routingServiceGroupName,
      "dynamic_routing_adapter.",
      properties
    );

    LOGGER.info("Connection created");
  }

  @Override
  public void close() {
    LOGGER.info("Closing connection");

    if (dynamicRoutingManager != null) {
      dynamicRoutingManager.close();
    }

    LOGGER.info("Connection closed");
  }

  @Override
  public void update(
    final Properties properties
  ) throws AdapterException {
    dynamicRoutingManager.update(properties);
  }
}
