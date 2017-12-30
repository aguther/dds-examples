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

  public static final String PREFIX = "test.";

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
        String.format("%sadministration.domain_id", PREFIX),
        Integer.toString(ADMINISTRATION_DOMAIN_ID)
    );
    properties.put(
        String.format("%sadministration.discovery.wait_time", PREFIX),
        Integer.toString(ADMINISTRATION_DISCOVERY_WAIT_TIME)
    );
    properties.put(
        String.format("%sadministration.request.timeout", PREFIX),
        Integer.toString(ADMINISTRATION_REQUEST_TIMEOUT)
    );
    properties.put(
        String.format("%sadministration.request.retry_delay", PREFIX),
        Integer.toString(ADMINISTRATION_REQUEST_RETRY_DELAY)
    );
    properties.put(
        String.format("%sdiscovery.domain_id", PREFIX),
        Integer.toString(DISCOVERY_DOMAIN_ID)
    );
    properties.put(
        String.format("%sconfiguration.domain_route_name", PREFIX),
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
        String.format("%sconfiguration.%s.allow_topic_name_filter", PREFIX, name),
        allowTopicFilter
    );
    properties.put(
        String.format("%sconfiguration.%s.deny_topic_name_filter", PREFIX, name),
        denyTopicFilter
    );
    properties.put(
        String.format("%sconfiguration.%s.allow_partition_name_filter", PREFIX, name),
        allowPartitionFilter
    );
    properties.put(
        String.format("%sconfiguration.%s.deny_partition_name_filter", PREFIX, name),
        denyPartitionFilter
    );
    properties.put(
        String.format("%sconfiguration.%s.qos.topic_route", PREFIX, name),
        qosTopicRoute
    );
    properties.put(
        String.format("%sconfiguration.%s.qos.input", PREFIX, name),
        qosInput
    );
    properties.put(
        String.format("%sconfiguration.%s.qos.output", PREFIX, name),
        qosOutput
    );
    properties.put(
        String.format("%sconfiguration.%s.unknown", PREFIX, name),
        "Unknown Value for Testing"
    );
  }

}
