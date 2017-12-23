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

import java.util.Properties;

public class PropertyFactory {

  public static final int ADMINISTRATION_DOMAIN_ID = 1;
  public static final int DISCOVERY_DOMAIN_ID = 2;
  public static final int ADMINISTRATION_DISCOVERY_WAIT_TIME = 250;
  public static final String CONFIGURATION_DOMAIN_ROUTE_NAME = "DomainRouteTest";

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
        "dynamic_routing_adapter.discovery.domain_id",
        Integer.toString(DISCOVERY_DOMAIN_ID)
    );
    properties.put(
        "dynamic_routing_adapter.configuration.domain_route_name",
        CONFIGURATION_DOMAIN_ROUTE_NAME
    );
    properties.put(
        "dynamic_routing_adapter.configuration.Shape.allow_topic_name_filter",
        "Square|T.*"
    );
    properties.put(
        "dynamic_routing_adapter.configuration.Shape.deny_topic_name_filter",
        "Circle"
    );
    properties.put(
        "dynamic_routing_adapter.configuration.Shape.allow_partition_name_filter",
        "A.*|B"
    );
    properties.put(
        "dynamic_routing_adapter.configuration.Shape.deny_partition_name_filter",
        ".*\\*|D"
    );
    properties.put(
        "dynamic_routing_adapter.configuration.Shape.qos.topic_route",
        "<publish_with_original_info>true</publish_with_original_info>"
    );
    properties.put(
        "dynamic_routing_adapter.configuration.Shape.qos.input",
        "<datareader_qos/>"
    );
    properties.put(
        "dynamic_routing_adapter.configuration.Shape.qos.output",
        "<datawriter_qos/>"
    );

    // return result
    return properties;
  }

}
