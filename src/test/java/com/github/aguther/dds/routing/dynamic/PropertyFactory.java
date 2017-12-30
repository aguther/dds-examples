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

import java.util.Properties;

public class PropertyFactory {

  public static final int ADMINISTRATION_DOMAIN_ID = 1;
  public static final int DISCOVERY_DOMAIN_ID = 2;
  public static final int ADMINISTRATION_DISCOVERY_WAIT_TIME = 250;
  public static final int ADMINISTRATION_REQUEST_TIMEOUT = 2500;
  public static final int ADMINISTRATION_REQUEST_RETRY_DELAY = 2500;

  public static final String CONFIGURATION_DOMAIN_ROUTE_NAME = "DomainRouteTest";

  public static final String CONFIGURATION_SHAPE_NAME = "Shape";
  public static final String CONFIGURATION_SHAPE_ALLOW_TOPIC_NAME_FILTER = "Square|T.*";
  public static final String CONFIGURATION_SHAPE_DENY_TOPIC_NAME_FILTER = "Circle";
  public static final String CONFIGURATION_SHAPE_ALLOW_PARTITION_NAME_FILTER = "A.*|B";
  public static final String CONFIGURATION_SHAPE_DENY_PARTITION_NAME_FILTER = ".*\\*|D";
  public static final String CONFIGURATION_SHAPE_QOS_TOPIC_ROUTE = "<publish_with_original_info>true</publish_with_original_info>";
  public static final String CONFIGURATION_SHAPE_QOS_INPUT = "<datareader_qos/>";
  public static final String CONFIGURATION_SHAPE_QOS_OUTPUT = "<datawriter_qos/>";

  public static final String CONFIGURATION_SAMPLE_NAME = "Sample";
  public static final String CONFIGURATION_SAMPLE_ALLOW_TOPIC_NAME_FILTER = "S.*";
  public static final String CONFIGURATION_SAMPLE_DENY_TOPIC_NAME_FILTER = "S.*Deny";
  public static final String CONFIGURATION_SAMPLE_ALLOW_PARTITION_NAME_FILTER = ".*";
  public static final String CONFIGURATION_SAMPLE_DENY_PARTITION_NAME_FILTER = ".*\\*";
  public static final String CONFIGURATION_SAMPLE_QOS_TOPIC_ROUTE = "<publish_with_original_info>true</publish_with_original_info>";
  public static final String CONFIGURATION_SAMPLE_QOS_INPUT = "<datareader_qos/>";
  public static final String CONFIGURATION_SAMPLE_QOS_OUTPUT = "<datawriter_qos/>";

  private PropertyFactory() {
  }

  public static Properties create() {
    // create properties
    Properties properties = new Properties();

    // fill data
    properties.put(
        "dynamic_routing_adapter.administration.domain_id",
        Integer.toString(ADMINISTRATION_DOMAIN_ID)
    );
    properties.put(
        "dynamic_routing_adapter.administration.discovery.wait_time",
        Integer.toString(ADMINISTRATION_DISCOVERY_WAIT_TIME)
    );
    properties.put(
        "dynamic_routing_adapter.administration.request.timeout",
        Integer.toString(ADMINISTRATION_REQUEST_TIMEOUT)
    );
    properties.put(
        "dynamic_routing_adapter.administration.request.retry_delay",
        Integer.toString(ADMINISTRATION_REQUEST_RETRY_DELAY)
    );
    properties.put(
        "dynamic_routing_adapter.discovery.domain_id",
        Integer.toString(DISCOVERY_DOMAIN_ID)
    );
    properties.put(
        "dynamic_routing_adapter.configuration.domain_route_name",
        CONFIGURATION_DOMAIN_ROUTE_NAME
    );

    addConfiguration(
        properties,
        CONFIGURATION_SHAPE_NAME,
        CONFIGURATION_SHAPE_ALLOW_TOPIC_NAME_FILTER,
        CONFIGURATION_SHAPE_DENY_TOPIC_NAME_FILTER,
        CONFIGURATION_SHAPE_ALLOW_PARTITION_NAME_FILTER,
        CONFIGURATION_SHAPE_DENY_PARTITION_NAME_FILTER,
        CONFIGURATION_SHAPE_QOS_TOPIC_ROUTE,
        CONFIGURATION_SHAPE_QOS_INPUT,
        CONFIGURATION_SHAPE_QOS_OUTPUT
    );
    addConfiguration(
        properties,
        CONFIGURATION_SAMPLE_NAME,
        CONFIGURATION_SAMPLE_ALLOW_TOPIC_NAME_FILTER,
        CONFIGURATION_SAMPLE_DENY_TOPIC_NAME_FILTER,
        CONFIGURATION_SAMPLE_ALLOW_PARTITION_NAME_FILTER,
        CONFIGURATION_SAMPLE_DENY_PARTITION_NAME_FILTER,
        CONFIGURATION_SAMPLE_QOS_TOPIC_ROUTE,
        CONFIGURATION_SAMPLE_QOS_INPUT,
        CONFIGURATION_SAMPLE_QOS_OUTPUT
    );

    // return result
    return properties;
  }

  private static void addConfiguration(
      Properties properties,
      String name,
      String allowTopicFilter,
      String denyTopicFilter,
      String allowPartitionFilter,
      String denyPartitionFilter,
      String qosTopicRoute,
      String qosInput,
      String qosOutput
  ) {
    properties.put(
        String.format("dynamic_routing_adapter.configuration.%s.allow_topic_name_filter", name),
        allowTopicFilter
    );
    properties.put(
        String.format("dynamic_routing_adapter.configuration.%s.deny_topic_name_filter", name),
        denyTopicFilter
    );
    properties.put(
        String.format("dynamic_routing_adapter.configuration.%s.allow_partition_name_filter", name),
        allowPartitionFilter
    );
    properties.put(
        String.format("dynamic_routing_adapter.configuration.%s.deny_partition_name_filter", name),
        denyPartitionFilter
    );
    properties.put(
        String.format("dynamic_routing_adapter.configuration.%s.qos.topic_route", name),
        qosTopicRoute
    );
    properties.put(
        String.format("dynamic_routing_adapter.configuration.%s.qos.input", name),
        qosInput
    );
    properties.put(
        String.format("dynamic_routing_adapter.configuration.%s.qos.output", name),
        qosOutput
    );
  }

}
