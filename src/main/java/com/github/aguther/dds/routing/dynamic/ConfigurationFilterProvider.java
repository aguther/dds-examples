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

package com.github.aguther.dds.routing.dynamic;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.aguther.dds.routing.dynamic.command.DynamicPartitionCommandProvider;
import com.github.aguther.dds.routing.dynamic.observer.Direction;
import com.github.aguther.dds.routing.dynamic.observer.DynamicPartitionObserverFilter;
import com.github.aguther.dds.routing.dynamic.observer.Session;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import com.google.common.base.Strings;
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
import org.apache.commons.text.StringSubstitutor;
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
public class ConfigurationFilterProvider implements DynamicPartitionObserverFilter, DynamicPartitionCommandProvider {

  private static final String PROPERTY_DOMAIN_ROUTE_NAME = "configuration.domain_route_name";

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationFilterProvider.class);

  private final Map<String, Configuration> configurations;
  private final Pattern patternConfigurationItem;
  private final String domainRouteName;

  /**
   * Instantiates a new configuration filter provider.
   *
   * @param properties the properties to configure this instance
   */
  public ConfigurationFilterProvider(
    final Properties properties
  ) {
    this("", properties);
  }

  /**
   * Instantiates a new configuration filter provider.
   *
   * @param properties the properties to configure this instance
   * @param prefix the prefix that should be used for the properties
   */
  public ConfigurationFilterProvider(
    final String prefix,
    final Properties properties
  ) {
    configurations = new HashMap<>();
    patternConfigurationItem = Pattern.compile(String.format(
      "%sconfiguration\\.([A-Za-z0-9_]*)\\.([A-Za-z0-9._]*)",
      prefix
    ));

    domainRouteName = StringSubstitutor.replace(
      properties.getProperty(String.format(
        "%s%s",
        prefix,
        PROPERTY_DOMAIN_ROUTE_NAME
      )),
      System.getenv()
    );
    loadConfiguration(properties);
  }

  /**
   * Loads the configuration from the properties.
   *
   * @param properties the properties to load
   */
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

  /**
   * Loads a configuration group from the properties.
   *
   * @param identifier identifier of the group
   * @param propertyName property name
   * @param propertyValue property value
   */
  private void loadProperty(
    final String identifier,
    final String propertyName,
    final String propertyValue) {
    // add new configuration if not yet known
    if (!configurations.containsKey(identifier)) {
      configurations.put(identifier, new Configuration());
    }

    // resolve any contained environment variables
    String propertyValueResolved = StringSubstitutor.replace(propertyValue, System.getenv());

    // store configuration
    switch (propertyName) {
      case "allow_topic_name_filter":
        configurations.get(identifier).setAllowTopicNameFilter(
          Pattern.compile(propertyValueResolved));
        break;
      case "deny_topic_name_filter":
        configurations.get(identifier).setDenyTopicNameFilter(
          Pattern.compile(propertyValueResolved));
        break;
      case "allow_partition_name_filter":
        configurations.get(identifier).setAllowPartitionNameFilter(
          Pattern.compile(propertyValueResolved));
        break;
      case "deny_partition_name_filter":
        configurations.get(identifier).setDenyPartitionNameFilter(
          Pattern.compile(propertyValueResolved));
        break;
      case "qos.topic_route":
        configurations.get(identifier).setTopicRouteQosQos(
          propertyValueResolved);
        break;
      case "qos.input":
        configurations.get(identifier).setQosInput(
          propertyValueResolved);
        break;
      case "qos.output":
        configurations.get(identifier).setQosOutput(
          propertyValueResolved);
        break;
      case "partition.transformation.regex":
        configurations.get(identifier).setPartitionTransformationRegex(
          propertyValueResolved);
        break;
      case "partition.transformation.replacement":
        configurations.get(identifier).setPartitionTransformationReplacement(
          propertyValueResolved);
        break;
      default:
        // unknown configuration
        break;
    }
  }

  /**
   * Logs the configuration.
   */
  private void logConfiguration() {
    // log loaded properties
    if (LOGGER.isDebugEnabled()) {
      for (Entry<String, Configuration> entry : configurations.entrySet()) {
        LOGGER.debug(
          "key='{}', allow_topic_name_filter='{}', deny_topic_name_filter='{}', allow_partition_name_filter='{}', deny_partition_name_filter='{}', qos.topic_route='{}', qos.input='{}', qos.output='{}', partition.transformation.regex='{}', partition.transformation.replacement='{}'",
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
          entry.getValue().getQosOutput(),
          entry.getValue().getPartitionTransformationRegex(),
          entry.getValue().getPartitionTransformationReplacement()
        );
      }
    }
  }

  /**
   * Returns the domain route name.
   *
   * @return domain route name
   */
  String getDomainRouteName() {
    return domainRouteName;
  }

  /**
   * Returns the current configuration.
   *
   * @return configuration as read-only map
   */
  Map<String, Configuration> getConfigurations() {
    return Collections.unmodifiableMap(configurations);
  }

  @Override
  public boolean ignorePublication(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final PublicationBuiltinTopicData data
  ) {
    checkNotNull(instanceHandle);
    checkNotNull(data);

    return ignorePublicationSubscription(
      instanceHandle,
      data.topic_name
    );
  }

  @Override
  public boolean ignoreSubscription(
    final DomainParticipant domainParticipant,
    final InstanceHandle_t instanceHandle,
    final SubscriptionBuiltinTopicData data
  ) {
    checkNotNull(instanceHandle);
    checkNotNull(data);

    return ignorePublicationSubscription(
      instanceHandle,
      data.topic_name
    );
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
      LOGGER.trace(
        "topic='{}', partition='{}', ignore='{}' (configuration not found)",
        topicName,
        partition,
        "true"
      );
      return true;
    }

    // check deny filter
    if (configuration.getDenyPartitionNameFilter() != null
      && configuration.getDenyPartitionNameFilter().matcher(partition).matches()) {
      LOGGER.trace(
        "topic='{}', partition='{}', ignore='{}' (deny partition filter matched)",
        topicName,
        partition,
        "true"
      );
      return true;
    }

    // check allow filter
    if (configuration.getAllowPartitionNameFilter() != null
      && !configuration.getAllowPartitionNameFilter().matcher(partition).matches()) {
      LOGGER.trace(
        "topic='{}', partition='{}', ignore='{}' (no match with allow partition filter)",
        topicName,
        partition,
        "true"
      );
      return true;
    }

    // do not ignore
    LOGGER.trace(
      "topic='{}', partition='{}', ignore='{}'",
      topicName,
      partition,
      "false"
    );
    return false;
  }

  private boolean ignorePublicationSubscription(
    InstanceHandle_t instanceHandle,
    String topicName
  ) {
    boolean result = (getMatchingConfiguration(topicName) == null);

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace(
        "instance='{}', ignore='{}' (configuration for topic '{}' {})",
        instanceHandle,
        result,
        topicName,
        result ? "not found" : "found"
      );
    }

    return result;
  }

  /**
   * Returns the first matching configuration. Deny filter takes precedence.
   *
   * @param topicName topic name
   * @return configuration if found, otherwise null
   */
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
      "%s(%s)-%s",
      session.getTopic(),
      session.getPartition(),
      session.getDirection()
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
    // set default partition for publisher and subscriber
    String publisherPartition = session.getPartition();
    String subscriberPartition = session.getPartition();

    // get matching configuration
    Configuration configuration = getMatchingConfiguration(session.getTopic());
    if (configuration != null
      && !Strings.isNullOrEmpty(configuration.getPartitionTransformationRegex())
      && !Strings.isNullOrEmpty(configuration.getPartitionTransformationReplacement())) {
      // generate transformed partition
      String transformedPartition = session.getPartition().replaceAll(
        configuration.getPartitionTransformationRegex(),
        configuration.getPartitionTransformationReplacement()
      );

      // depending on direction, replace partition
      if (session.getDirection() == Direction.OUT) {
        subscriberPartition = transformedPartition;
      } else {
        publisherPartition = transformedPartition;
      }
    }

    return String.format(
      "<session name=\"%1$s\" enabled=\"true\"><publisher_qos><partition><name><element>%2$s</element></name></partition></publisher_qos><subscriber_qos><partition><name><element>%3$s</element></name></partition></subscriber_qos></session>",
      getSessionName(session),
      publisherPartition,
      subscriberPartition
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
      "<topic_route name=\"%1$s\" enabled=\"true\">%5$s<input participant=\"%2$d\"><topic_name>%3$s</topic_name><registered_type_name>%4$s</registered_type_name>%6$s</input><output><topic_name>%3$s</topic_name><registered_type_name>%4$s</registered_type_name>%7$s</output></topic_route>",
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
