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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationFilter implements DynamicPartitionObserverFilter, DynamicPartitionCommanderProvider {

  private static final Logger log;

  private static final String PROPERTY_DOMAIN_ROUTE_NAME;

  static {
    log = LoggerFactory.getLogger(ConfigurationFilter.class);

    PROPERTY_DOMAIN_ROUTE_NAME = "dynamic_routing_adapter.configuration.domain_route_name";
  }

  private final Map<String, Configuration> configurations;
  private final Pattern patternConfigurationItem;

  private final String domainRouteName;

  public ConfigurationFilter(
      Properties properties
  ) {
    configurations = new HashMap<>();
    patternConfigurationItem = Pattern.compile("dynamic_routing_adapter\\.configuration\\.(.*)\\.(.*)");

    domainRouteName = properties.getProperty(PROPERTY_DOMAIN_ROUTE_NAME);
    loadConfiguration(properties);

    if (log.isDebugEnabled()) {
      for (Entry<String, Configuration> entry : configurations.entrySet()) {
        log.debug(
            "key='{}', allow_topic='{}', deny_topic='{}', allow_partition='{}', deny_partition='{}', readerQos='{}', writerQos='{}'",
            entry.getKey(),
            entry.getValue().getAllowTopicNameFilter() != null ?
                entry.getValue().getAllowTopicNameFilter().pattern() : "",
            entry.getValue().getDenyTopicNameFilter() != null ?
                entry.getValue().getDenyTopicNameFilter().pattern() : "",
            entry.getValue().getAllowPartitionNameFilter() != null ?
                entry.getValue().getAllowPartitionNameFilter().pattern() : "",
            entry.getValue().getDenyPartitionNameFilter() != null ?
                entry.getValue().getDenyPartitionNameFilter().pattern() : "",
            entry.getValue().getDatareaderQos(),
            entry.getValue().getDatawriterQos()
        );
      }
    }
  }

  private void loadConfiguration(
      Properties properties
  ) {
    for (Entry<Object, Object> entry : properties.entrySet()) {
      // run the matcher
      Matcher entryMatch = patternConfigurationItem.matcher(entry.getKey().toString());

      // result valid?
      if (!entryMatch.matches()) {
        continue;
      }

      // get identifier
      String identifier = entryMatch.group(1);

      // add new configuration if not yet known
      if (!configurations.containsKey(identifier)) {
        configurations.put(identifier, new Configuration());
      }

      // store configuration
      switch (entryMatch.group(2)) {
        case "allow_topic_name_filter":
          configurations.get(identifier).setAllowTopicNameFilter(
              Pattern.compile(entry.getValue().toString()));
          break;
        case "deny_topic_name_filter":
          configurations.get(identifier).setDenyTopicNameFilter(
              Pattern.compile(entry.getValue().toString()));
          break;
        case "allow_partition_name_filter":
          configurations.get(identifier).setAllowPartitionNameFilter(
              Pattern.compile(entry.getValue().toString()));
          break;
        case "deny_partition_name_filter":
          configurations.get(identifier).setDenyPartitionNameFilter(
              Pattern.compile(entry.getValue().toString()));
          break;
        case "datareader_qos":
          configurations.get(identifier).setDatareaderQos(
              entry.getValue().toString());
          break;
        case "datawriter_qos":
          configurations.get(identifier).setDatawriterQos(
              entry.getValue().toString());
          break;
        default:
          // unknown configuration
          break;
      }
    }
  }

  @Override
  public boolean ignorePublication(
      DomainParticipant domainParticipant,
      InstanceHandle_t instanceHandle,
      PublicationBuiltinTopicData data
  ) {
    return (getMatchingConfiguration(data.topic_name) == null);
  }

  @Override
  public boolean ignoreSubscription(
      DomainParticipant domainParticipant,
      InstanceHandle_t instanceHandle,
      SubscriptionBuiltinTopicData data
  ) {
    return (getMatchingConfiguration(data.topic_name) == null);
  }

  @Override
  public boolean ignorePartition(
      String partition
  ) {
    for (Configuration configuration : configurations.values()) {
      if (configuration.getDenyPartitionNameFilter() != null
          && configuration.getDenyPartitionNameFilter().matcher(partition).matches()) {
        continue;
      }
      if (configuration.getAllowPartitionNameFilter() != null
          && configuration.getAllowPartitionNameFilter().matcher(partition).matches()) {
        return false;
      }
    }
    return true;
  }

  private Configuration getMatchingConfiguration(
      String topicName
  ) {
    for (Configuration configuration : configurations.values()) {
      if (configuration.getDenyTopicNameFilter() != null
          && configuration.getDenyTopicNameFilter().matcher(topicName).matches()) {
        continue;
      }
      if (configuration.getAllowTopicNameFilter() != null
          && configuration.getAllowTopicNameFilter().matcher(topicName).matches()) {
        return configuration;
      }
    }
    return null;
  }

  @Override
  public String getSessionParent(
      Session session
  ) {
    return domainRouteName;
  }

  @Override
  public String getSessionName(
      Session session
  ) {
    return String.format(
        "%s(%s)",
        session.getTopic(),
        session.getPartition()
    );
  }

  @Override
  public String getSessionEntityName(
      Session session
  ) {
    return String.format(
        "%s::%s",
        getSessionParent(session),
        getSessionName(session)
    );
  }

  @Override
  public String getSessionConfiguration(
      Session session
  ) {
    return String.format(
        "str://\"<session name=\"%1$s\" enabled=\"true\"><publisher_qos><partition><name><element>%2$s</element></name></partition></publisher_qos><subscriber_qos><partition><name><element>%2$s</element></name></partition></subscriber_qos></session>\"",
        getSessionName(session),
        session.getPartition()
    );
  }

  @Override
  public String getTopicRouteName(
      Session session,
      TopicRoute topicRoute
  ) {
    return topicRoute.getDirection().toString();
  }

  @Override
  public String getTopicRouteEntityName(
      Session session,
      TopicRoute topicRoute
  ) {
    return String.format(
        "%s::%s",
        getSessionEntityName(session),
        getTopicRouteName(session, topicRoute)
    );
  }

  @Override
  public String getTopicRouteConfiguration(
      Session session,
      TopicRoute topicRoute
  ) {
    Configuration configuration = getMatchingConfiguration(session.getTopic());
    checkNotNull(configuration);

    return String.format(
        "str://\"<topic_route name=\"%1$s\" enabled=\"true\"><input participant=\"%2$d\"><creation_mode>IMMEDIATE</creation_mode><topic_name>%3$s</topic_name><registered_type_name>%4$s</registered_type_name>%5$s</input><output><creation_mode>IMMEDIATE</creation_mode><topic_name>%3$s</topic_name><registered_type_name>%4$s</registered_type_name>%6$s</output></topic_route>\"",
        getTopicRouteName(session, topicRoute),
        topicRoute.getDirection() == Direction.OUT ? 1 : 2,
        session.getTopic(),
        topicRoute.getType(),
        configuration.getDatareaderQos(),
        configuration.getDatawriterQos()
    );
  }
}
