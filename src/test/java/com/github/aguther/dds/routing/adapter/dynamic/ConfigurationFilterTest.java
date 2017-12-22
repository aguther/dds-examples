package com.github.aguther.dds.routing.adapter.dynamic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;

public class ConfigurationFilterTest {

  private ConfigurationFilter configurationFilter;

  @Before
  public void setUp() throws Exception {
    // create properties
    Properties properties = new Properties();
    properties.put(
        "dynamic_routing_adapter.administration.domain_id",
        "0"
    );
    properties.put(
        "dynamic_routing_adapter.administration.discovery.wait_time",
        "10000"
    );
    properties.put(
        "dynamic_routing_adapter.discovery.domain_id",
        "0"
    );
    properties.put(
        "dynamic_routing_adapter.configuration.domain_route_name",
        "Default"
    );
    properties.put(
        "dynamic_routing_adapter.configuration.Shape.allow_topic_name_filter",
        "Square|.*"
    );
    properties.put(
        "dynamic_routing_adapter.configuration.Shape.deny_topic_name_filter",
        "Circle"
    );
    properties.put(
        "dynamic_routing_adapter.configuration.Shape.allow_partition_name_filter",
        "A|B|C|D"
    );
    properties.put(
        "dynamic_routing_adapter.configuration.Shape.deny_partition_name_filter",
        ".*\\*"
    );
    properties.put(
        "dynamic_routing_adapter.configuration.Shape.qos.topic_route",
        "<publish_with_original_info>true</publish_with_original_info>"
    );
    properties.put("dynamic_routing_adapter.configuration.Shape.qos.input",
        "<datareader_qos/>"
    );
    properties.put("dynamic_routing_adapter.configuration.Shape.qos.output",
        "<datawriter_qos/>"
    );

    // create configuration filter
    configurationFilter = new ConfigurationFilter(properties);
  }

  @Test
  public void ignorePublication() {
    PublicationBuiltinTopicData publicationBuiltinTopicData = new PublicationBuiltinTopicData();

    publicationBuiltinTopicData.topic_name = "Square";
    assertFalse(configurationFilter.ignorePublication(null, null, publicationBuiltinTopicData));

    publicationBuiltinTopicData.topic_name = "Circle";
    assertTrue(configurationFilter.ignorePublication(null, null, publicationBuiltinTopicData));

    publicationBuiltinTopicData.topic_name = "Triangle";
    assertFalse(configurationFilter.ignorePublication(null, null, publicationBuiltinTopicData));
  }

  @Test
  public void ignoreSubscription() {
    SubscriptionBuiltinTopicData subscriptionBuiltinTopicData = new SubscriptionBuiltinTopicData();

    subscriptionBuiltinTopicData.topic_name = "Square";
    assertFalse(configurationFilter.ignoreSubscription(null, null, subscriptionBuiltinTopicData));

    subscriptionBuiltinTopicData.topic_name = "Circle";
    assertTrue(configurationFilter.ignoreSubscription(null, null, subscriptionBuiltinTopicData));

    subscriptionBuiltinTopicData.topic_name = "Triangle";
    assertFalse(configurationFilter.ignoreSubscription(null, null, subscriptionBuiltinTopicData));
  }

  @Test
  public void ignorePartition() {
  }

  @Test
  public void getSessionParent() {
  }

  @Test
  public void getSessionName() {
  }

  @Test
  public void getSessionEntityName() {
  }

  @Test
  public void getSessionConfiguration() {
  }

  @Test
  public void getTopicRouteName() {
  }

  @Test
  public void getTopicRouteEntityName() {
  }

  @Test
  public void getTopicRouteConfiguration() {
  }
}