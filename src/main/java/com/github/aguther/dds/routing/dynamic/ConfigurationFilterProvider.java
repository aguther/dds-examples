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

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.aguther.dds.routing.dynamic.command.DynamicPartitionCommanderProvider;
import com.github.aguther.dds.routing.dynamic.observer.DynamicPartitionObserverFilter;
import com.github.aguther.dds.routing.dynamic.observer.Session;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute.Direction;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a filter and provider to be used with DynamicPartitionObserver and DynamicPartitionCommander.
 *
 * The configuration is provided via properties in the routing service configuration. It allows multiple configurations
 * with different allow and deny filters for topic and partition.
 *
 * Deny filters take precedence over allow filters.
 *
 * WARNING: It must be ensured that the configuration filters are disjoint (combination of allow and deny filters)
 * otherwise the results are unpredictable.
 */
public class ConfigurationFilterProvider implements DynamicPartitionObserverFilter, DynamicPartitionCommanderProvider {

  private static final String PROPERTY_DOMAIN_ROUTE_NAME = "configuration.domain_route_name";

  private static final Logger log = LoggerFactory.getLogger(
      ConfigurationFilterProvider.class);

  private final Map<String, Configuration> configurations;
  private final Pattern patternConfigurationItem;
  private final String domainRouteName;

  public ConfigurationFilterProvider(
      final Properties properties
  ) {
    this("", properties);
  }

  public ConfigurationFilterProvider(
      final String prefix,
      final Properties properties
  ) {
    configurations = new HashMap<>();
    patternConfigurationItem = Pattern.compile(String.format(
        "%sconfiguration\\.([A-Za-z0-9_]*)\\.([A-Za-z0-9._]*)",
        prefix
    ));

    domainRouteName = properties.getProperty(String.format(
        "%s%s",
        prefix,
        PROPERTY_DOMAIN_ROUTE_NAME
    ));
    loadConfiguration(properties);
  }

  private void loadConfiguration(
      final Properties properties
  ) {
    for (Entry<Object, Object> entry : properties.entrySet()) {
      // run the matcher
      Matcher entryMatch = patternConfigurationItem.matcher(entry.getKey().toString());

      // result valid?
      if (!entryMatch.matches()) {
        continue;
      }

      // load property
      loadProperty(
          entryMatch.group(1),
          entryMatch.group(2),
          entry.getValue().toString()
      );
    }

    // log configuration
    logConfiguration();
  }

  private void loadProperty(
      final String identifier,
      final String propertyName,
      final String propertyValue) {
    // add new configuration if not yet known
    if (!configurations.containsKey(identifier)) {
      configurations.put(identifier, new Configuration());
    }

    // store configuration
    switch (propertyName) {
      case "allow_topic_name_filter":
        configurations.get(identifier).setAllowTopicNameFilter(
            Pattern.compile(propertyValue));
        break;
      case "deny_topic_name_filter":
        configurations.get(identifier).setDenyTopicNameFilter(
            Pattern.compile(propertyValue));
        break;
      case "allow_partition_name_filter":
        configurations.get(identifier).setAllowPartitionNameFilter(
            Pattern.compile(propertyValue));
        break;
      case "deny_partition_name_filter":
        configurations.get(identifier).setDenyPartitionNameFilter(
            Pattern.compile(propertyValue));
        break;
      case "qos.topic_route":
        configurations.get(identifier).setTopicRouteQosQos(
            propertyValue);
        break;
      case "qos.input":
        configurations.get(identifier).setQosInput(
            propertyValue);
        break;
      case "qos.output":
        configurations.get(identifier).setQosOutput(
            propertyValue);
        break;
      default:
        // unknown configuration
        break;
    }
  }

  private void logConfiguration() {
    // log loaded properties
    if (log.isDebugEnabled()) {
      for (Entry<String, Configuration> entry : configurations.entrySet()) {
        log.debug(
            "key='{}', allow_topic_name_filter='{}', deny_topic_name_filter='{}', allow_partition_name_filter='{}', deny_partition_name_filter='{}', qos.topic_route='{}', qos.input='{}', qos.output='{}'",
            entry.getKey(),
            entry.getValue().getAllowTopicNameFilter() != null ?
                entry.getValue().getAllowTopicNameFilter().pattern() : "",
            entry.getValue().getDenyTopicNameFilter() != null ?
                entry.getValue().getDenyTopicNameFilter().pattern() : "",
            entry.getValue().getAllowPartitionNameFilter() != null ?
                entry.getValue().getAllowPartitionNameFilter().pattern() : "",
            entry.getValue().getDenyPartitionNameFilter() != null ?
                entry.getValue().getDenyPartitionNameFilter().pattern() : "",
            entry.getValue().getQosTopicRoute(),
            entry.getValue().getQosInput(),
            entry.getValue().getQosOutput()
        );
      }
    }
  }

  String getDomainRouteName() {
    return domainRouteName;
  }

  Map<String, Configuration> getConfigurations() {
    return Collections.unmodifiableMap(configurations);
  }

  @Override
  public boolean ignorePublication(
      final DomainParticipant domainParticipant,
      final InstanceHandle_t instanceHandle,
      final PublicationBuiltinTopicData data
  ) {
    checkNotNull(data);
    return (getMatchingConfiguration(data.topic_name) == null);
  }

  @Override
  public boolean ignoreSubscription(
      final DomainParticipant domainParticipant,
      final InstanceHandle_t instanceHandle,
      final SubscriptionBuiltinTopicData data
  ) {
    checkNotNull(data);
    return (getMatchingConfiguration(data.topic_name) == null);
  }

  @Override
  public boolean ignorePartition(
      final String topicName,
      final String partition
  ) {
    // get matching configuration
    Configuration configuration = getMatchingConfiguration(topicName);

    // if we do not find a matching configuration we should ignore the partition
    if (configuration == null) {
      return true;
    }

    // when a deny filter is available check if it matches
    return (
        configuration.getDenyPartitionNameFilter() != null
            && configuration.getDenyPartitionNameFilter().matcher(partition).matches())
        || (
        configuration.getAllowPartitionNameFilter() != null
            && !configuration.getAllowPartitionNameFilter().matcher(partition).matches());
  }

  private Configuration getMatchingConfiguration(
      final String topicName
  ) {
    for (Configuration configuration : configurations.values()) {
      // when a deny filter is available check if it matches
      if (configuration.getDenyTopicNameFilter() != null
          && configuration.getDenyTopicNameFilter().matcher(topicName).matches()) {
        continue;
      }
      // when no allow filter is available allow all topics, otherwise check if it matches
      if (configuration.getAllowTopicNameFilter() == null
          || configuration.getAllowTopicNameFilter().matcher(topicName).matches()) {
        return configuration;
      }
    }
    return null;
  }

  @Override
  public String getSessionParent(
      final Session session
  ) {
    return domainRouteName;
  }

  @Override
  public String getSessionName(
      final Session session
  ) {
    return String.format(
        "%s(%s)",
        session.getTopic(),
        session.getPartition()
    );
  }

  @Override
  public String getSessionEntityName(
      final Session session
  ) {
    return String.format(
        "%s::%s",
        getSessionParent(session),
        getSessionName(session)
    );
  }

  @Override
  public String getSessionConfiguration(
      final Session session
  ) {
    return String.format(
        "str://\"<session name=\"%1$s\" enabled=\"true\"><publisher_qos><partition><name><element>%2$s</element></name></partition></publisher_qos><subscriber_qos><partition><name><element>%2$s</element></name></partition></subscriber_qos></session>\"",
        getSessionName(session),
        session.getPartition()
    );
  }

  @Override
  public String getTopicRouteName(
      final Session session,
      final TopicRoute topicRoute
  ) {
    return topicRoute.getDirection().toString();
  }

  @Override
  public String getTopicRouteEntityName(
      final Session session,
      final TopicRoute topicRoute
  ) {
    return String.format(
        "%s::%s",
        getSessionEntityName(session),
        getTopicRouteName(session, topicRoute)
    );
  }

  @Override
  public String getTopicRouteConfiguration(
      final Session session,
      final TopicRoute topicRoute
  ) {
    Configuration configuration = getMatchingConfiguration(session.getTopic());
    checkNotNull(configuration);

    return String.format(
        "str://\"<topic_route name=\"%1$s\" enabled=\"true\">%5$s<input participant=\"%2$d\"><topic_name>%3$s</topic_name><registered_type_name>%4$s</registered_type_name>%6$s</input><output><topic_name>%3$s</topic_name><registered_type_name>%4$s</registered_type_name>%7$s</output></topic_route>\"",
        getTopicRouteName(session, topicRoute),
        topicRoute.getDirection() == Direction.OUT ? 1 : 2,
        session.getTopic(),
        topicRoute.getType(),
        configuration.getQosTopicRoute(),
        configuration.getQosInput(),
        configuration.getQosOutput()
    );
  }
}
