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

package io.github.aguther.dds.routing.adapter.dynamic;

import com.rti.routingservice.adapter.Adapter;
import com.rti.routingservice.adapter.Connection;
import com.rti.routingservice.adapter.StreamReaderListener;
import com.rti.routingservice.adapter.infrastructure.AdapterException;
import com.rti.routingservice.adapter.infrastructure.Version;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class implements an adapter to provide a function to dynamically route topics based on their partition without
 * loosing their origin (this happens when using asterisk or multiple partitions).
 */
public class DynamicRoutingAdapter implements Adapter {

  private static final Logger LOGGER = LogManager.getLogger(DynamicRoutingAdapter.class);

  public DynamicRoutingAdapter(
    final Properties properties
  ) {
    // do nothing but logging of received properties
    if (LOGGER.isDebugEnabled()) {
      for (String key : properties.stringPropertyNames()) {
        LOGGER.debug(
          "Property key='{}', value='{}'",
          key,
          properties.getProperty(key)
        );
      }
    }
  }

  @Override
  public Connection createConnection(
    final String routingServiceName,
    final String routingServiceGroupName,
    final StreamReaderListener inputStreamDiscoveryListener,
    final StreamReaderListener outputStreamDiscoveryListener,
    final Properties properties
  ) throws AdapterException {
    try {
      return new DynamicRoutingConnection(
        routingServiceName,
        routingServiceGroupName,
        properties
      );
    } catch (Exception ex) {
      throw new AdapterException(0, "Failed to initialize adapter", ex);
    }
  }

  @Override
  public void deleteConnection(
    final Connection connection
  ) throws AdapterException {
    try {
      if (connection instanceof DynamicRoutingConnection) {
        ((DynamicRoutingConnection) connection).close();
      }
    } catch (Exception ex) {
      throw new AdapterException(0, "Failed to shutdown adapter", ex);
    }
  }

  @Override
  public Version getVersion() {
    return new Version(1, 0, 0, 0);
  }
}
