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

import com.github.aguther.dds.routing.adapter.empty.EmptySession;
import com.github.aguther.dds.routing.adapter.empty.EmptyStreamReader;
import com.github.aguther.dds.routing.adapter.empty.EmptyStreamWriter;
import com.github.aguther.dds.routing.dynamic.DynamicRouting;
import com.rti.routingservice.adapter.DiscoveryConnection;
import com.rti.routingservice.adapter.Session;
import com.rti.routingservice.adapter.StreamReader;
import com.rti.routingservice.adapter.StreamReaderListener;
import com.rti.routingservice.adapter.StreamWriter;
import com.rti.routingservice.adapter.infrastructure.AdapterException;
import com.rti.routingservice.adapter.infrastructure.StreamInfo;
import java.io.Closeable;
import java.util.Properties;
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
public class DynamicRoutingConnection implements DiscoveryConnection, Closeable {

  private static final Logger log = LoggerFactory.getLogger(DynamicRoutingConnection.class);

  private DynamicRouting dynamicRouting;

  DynamicRoutingConnection(
      final String routingServiceName,
      final String routingServiceGroupName,
      final Properties properties
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

    dynamicRouting = new DynamicRouting(
        routingServiceName,
        routingServiceGroupName,
        "dynamic_routing_adapter.",
        properties
    );

    log.info("Connection created");
  }

  @Override
  public void close() {
    log.info("Closing connection");

    if (dynamicRouting != null) {
      dynamicRouting.close();
    }

    log.info("Connection closed");
  }

  @Override
  public Session createSession(
      final Properties properties
  ) throws AdapterException {
    return new EmptySession();
  }

  @Override
  public void deleteSession(
      final Session session
  ) throws AdapterException {
    // do nothing
  }

  @Override
  public StreamReader createStreamReader(
      final Session session,
      final StreamInfo streamInfo,
      final Properties properties,
      final StreamReaderListener streamReaderListener
  ) throws AdapterException {
    return new EmptyStreamReader();
  }

  @Override
  public void deleteStreamReader(
      final StreamReader streamReader
  ) throws AdapterException {
    // do nothing
  }

  @Override
  public StreamWriter createStreamWriter(
      final Session session,
      final StreamInfo streamInfo,
      final Properties properties
  ) throws AdapterException {
    return new EmptyStreamWriter();
  }

  @Override
  public void deleteStreamWriter(
      final StreamWriter streamWriter
  ) throws AdapterException {
    // do nothing
  }

  @Override
  public Properties getAttributes() throws AdapterException {
    return dynamicRouting.getProperties();
  }

  @Override
  public void update(
      final Properties properties
  ) throws AdapterException {
    dynamicRouting.update(properties);
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
      final Object o
  ) throws AdapterException {
    throw new AdapterException(0, "Operation not supported");
  }

  @Override
  public void deleteTypeRepresentation(
      final Object o
  ) throws AdapterException {
    // do nothing
  }
}
