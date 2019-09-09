/*
 * MIT License
 *
 * Copyright (c) 2018 Andreas Guther
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;

import com.github.aguther.dds.routing.dynamic.observer.Direction;
import com.github.aguther.dds.routing.dynamic.observer.Session;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
  PublicationBuiltinTopicData.class,
  SubscriptionBuiltinTopicData.class,
})
@SuppressStaticInitializationFor({
  "com.rti.dds.publication.builtin.PublicationBuiltinTopicData",
  "com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData",
  "com.rti.dds.topic.AbstractBuiltinTopicData",
})
public class ConfigurationFilterProviderTest {

  private ConfigurationFilterProvider configurationFilterProvider;

  @Before
  public void setUp() {
    // create configuration filter
    configurationFilterProvider = new ConfigurationFilterProvider(
      PropertyFactory.PREFIX,
      PropertyFactory.create()
    );
  }

  @After
  public void tearDown() {
    configurationFilterProvider = null;
  }

  @Test
  public void testDefaultPrefix() {
    ConfigurationFilterProvider filterProvider = new ConfigurationFilterProvider(
      PropertyFactory.create("")
    );
    checkConfiguration(filterProvider);
  }

  @Test
  public void testLoadConfiguration() {
    checkConfiguration(configurationFilterProvider);
  }

  private void checkConfiguration(
    final ConfigurationFilterProvider configurationFilterProvider
  ) {
    assertEquals(
      PropertyFactory.CONFIGURATION_DOMAIN_ROUTE_NAME,
      configurationFilterProvider.getDomainRouteName()
    );

    Map<String, Configuration> configurations = configurationFilterProvider.getConfigurations();

    Configuration shapeConfiguration = configurations.get(PropertyFactory.CONFIGURATION_SHAPE_NAME);
    assertEquals(
      PropertyFactory.CONFIGURATION_SHAPE_ALLOW_TOPIC_NAME_FILTER,
      shapeConfiguration.getAllowTopicNameFilter().pattern()
    );
    assertEquals(
      PropertyFactory.CONFIGURATION_SHAPE_DENY_TOPIC_NAME_FILTER,
      shapeConfiguration.getDenyTopicNameFilter().pattern()
    );
    assertEquals(
      PropertyFactory.CONFIGURATION_SHAPE_ALLOW_PARTITION_NAME_FILTER,
      shapeConfiguration.getAllowPartitionNameFilter().pattern()
    );
    assertEquals(
      PropertyFactory.CONFIGURATION_SHAPE_DENY_PARTITION_NAME_FILTER,
      shapeConfiguration.getDenyPartitionNameFilter().pattern()
    );
    assertEquals(
      PropertyFactory.CONFIGURATION_SHAPE_QOS_TOPIC_ROUTE,
      shapeConfiguration.getQosTopicRoute()
    );
    assertEquals(
      PropertyFactory.CONFIGURATION_SHAPE_QOS_INPUT,
      shapeConfiguration.getQosInput()
    );
    assertEquals(
      PropertyFactory.CONFIGURATION_SHAPE_QOS_OUTPUT,
      shapeConfiguration.getQosOutput()
    );

    Configuration testConfiguration = configurations.get(PropertyFactory.CONFIGURATION_SAMPLE_NAME);
    assertEquals(
      PropertyFactory.CONFIGURATION_SAMPLE_ALLOW_TOPIC_NAME_FILTER,
      testConfiguration.getAllowTopicNameFilter().pattern()
    );
    assertEquals(
      PropertyFactory.CONFIGURATION_SAMPLE_DENY_TOPIC_NAME_FILTER,
      testConfiguration.getDenyTopicNameFilter().pattern()
    );
    assertEquals(
      PropertyFactory.CONFIGURATION_SAMPLE_ALLOW_PARTITION_NAME_FILTER,
      testConfiguration.getAllowPartitionNameFilter().pattern()
    );
    assertEquals(
      PropertyFactory.CONFIGURATION_SAMPLE_DENY_PARTITION_NAME_FILTER,
      testConfiguration.getDenyPartitionNameFilter().pattern()
    );
    assertEquals(
      PropertyFactory.CONFIGURATION_SAMPLE_QOS_TOPIC_ROUTE,
      testConfiguration.getQosTopicRoute()
    );
    assertEquals(
      PropertyFactory.CONFIGURATION_SAMPLE_QOS_INPUT,
      testConfiguration.getQosInput()
    );
    assertEquals(
      PropertyFactory.CONFIGURATION_SAMPLE_QOS_OUTPUT,
      testConfiguration.getQosOutput()
    );
  }

  @Test
  public void testIgnorePublication() {
    PublicationBuiltinTopicData publicationBuiltinTopicData = mock(PublicationBuiltinTopicData.class);

    publicationBuiltinTopicData.topic_name = "Square";
    assertFalse(configurationFilterProvider.ignorePublication(
      null,
      InstanceHandle_t.HANDLE_NIL,
      publicationBuiltinTopicData
    ));
    publicationBuiltinTopicData.topic_name = "Triangle";
    assertFalse(configurationFilterProvider.ignorePublication(
      null,
      InstanceHandle_t.HANDLE_NIL,
      publicationBuiltinTopicData
    ));

    publicationBuiltinTopicData.topic_name = "Circle";
    assertTrue(configurationFilterProvider.ignorePublication(
      null,
      InstanceHandle_t.HANDLE_NIL,
      publicationBuiltinTopicData
    ));
    publicationBuiltinTopicData.topic_name = "OtherTopic";
    assertTrue(configurationFilterProvider.ignorePublication(
      null,
      InstanceHandle_t.HANDLE_NIL,
      publicationBuiltinTopicData
    ));
  }

  @Test
  public void testIgnoreSubscription() {
    SubscriptionBuiltinTopicData subscriptionBuiltinTopicData = mock(SubscriptionBuiltinTopicData.class);

    subscriptionBuiltinTopicData.topic_name = "Square";
    assertFalse(configurationFilterProvider.ignoreSubscription(
      null,
      InstanceHandle_t.HANDLE_NIL,
      subscriptionBuiltinTopicData
    ));
    subscriptionBuiltinTopicData.topic_name = "Triangle";
    assertFalse(configurationFilterProvider.ignoreSubscription(
      null,
      InstanceHandle_t.HANDLE_NIL,
      subscriptionBuiltinTopicData
    ));

    subscriptionBuiltinTopicData.topic_name = "Circle";
    assertTrue(configurationFilterProvider.ignoreSubscription(
      null,
      InstanceHandle_t.HANDLE_NIL,
      subscriptionBuiltinTopicData
    ));
  }

  @Test
  public void testIgnorePartition() {
    String topic = "Square";

    assertFalse(configurationFilterProvider.ignorePartition(topic, "A"));
    assertFalse(configurationFilterProvider.ignorePartition(topic, "ATest"));
    assertFalse(configurationFilterProvider.ignorePartition(topic, "B"));

    assertFalse(configurationFilterProvider.ignorePartition("Sample", "Partition"));
    assertFalse(configurationFilterProvider.ignorePartition("Sample", "Partition"));
    assertFalse(configurationFilterProvider.ignorePartition("SomeOtherTopic", "Partition"));

    assertTrue(configurationFilterProvider.ignorePartition(topic, "*"));
    assertTrue(configurationFilterProvider.ignorePartition(topic, "A*"));
    assertTrue(configurationFilterProvider.ignorePartition(topic, "D"));
    assertTrue(configurationFilterProvider.ignorePartition(topic, "D*"));
    assertTrue(configurationFilterProvider.ignorePartition(topic, "C"));

    assertTrue(configurationFilterProvider.ignorePartition("Sample", "Partition*"));
    assertTrue(configurationFilterProvider.ignorePartition("Sample", "*"));
    assertTrue(configurationFilterProvider.ignorePartition("SampleDeny", "A"));
    assertTrue(configurationFilterProvider.ignorePartition("SampleDeny", "B"));
    assertTrue(configurationFilterProvider.ignorePartition("SampleDeny", "*"));
  }

  @Test
  public void testGetSessionParent() {
    assertEquals(
      PropertyFactory.CONFIGURATION_DOMAIN_ROUTE_NAME,
      configurationFilterProvider.getSessionParent(new Session(Direction.OUT, "Square", "A"))
    );
  }

  @Test
  public void testGetSessionNameOut() {
    assertEquals(
      "Square()-OUT",
      configurationFilterProvider.getSessionName(new Session(Direction.OUT, "Square", ""))
    );
    assertEquals(
      "Square(A)-OUT",
      configurationFilterProvider.getSessionName(new Session(Direction.OUT, "Square", "A"))
    );
  }

  @Test
  public void testGetSessionNameIn() {
    assertEquals(
      "Square()-IN",
      configurationFilterProvider.getSessionName(new Session(Direction.IN, "Square", ""))
    );
    assertEquals(
      "Square(A)-IN",
      configurationFilterProvider.getSessionName(new Session(Direction.IN, "Square", "A"))
    );
  }

  @Test
  public void testGetSessionEntityNameOut() {
    assertEquals(
      String.format(
        "%s::%s",
        PropertyFactory.CONFIGURATION_DOMAIN_ROUTE_NAME,
        "Square()-OUT"
      ),
      configurationFilterProvider.getSessionEntityName(new Session(Direction.OUT, "Square", ""))
    );
    assertEquals(
      String.format(
        "%s::%s",
        PropertyFactory.CONFIGURATION_DOMAIN_ROUTE_NAME,
        "Square(A)-OUT"
      ),
      configurationFilterProvider.getSessionEntityName(new Session(Direction.OUT, "Square", "A"))
    );
  }

  @Test
  public void testGetSessionEntityNameIn() {
    assertEquals(
      String.format(
        "%s::%s",
        PropertyFactory.CONFIGURATION_DOMAIN_ROUTE_NAME,
        "Square()-IN"
      ),
      configurationFilterProvider.getSessionEntityName(new Session(Direction.IN, "Square", ""))
    );
    assertEquals(
      String.format(
        "%s::%s",
        PropertyFactory.CONFIGURATION_DOMAIN_ROUTE_NAME,
        "Square(A)-IN"
      ),
      configurationFilterProvider.getSessionEntityName(new Session(Direction.IN, "Square", "A"))
    );
  }

  @Test
  public void testGetSessionConfiguration() {
    Session session = new Session(Direction.OUT, "Square", "A");
    String configuration = configurationFilterProvider.getSessionConfiguration(session);

    assertTrue(configuration.contains("Square"));
    assertTrue(configuration.contains("A"));
  }

  @Test
  public void testGetTopicRouteName() {
    assertEquals(
      Direction.IN.toString(),
      configurationFilterProvider.getTopicRouteName(
        new Session(Direction.IN, "Square", ""),
        new TopicRoute(Direction.IN, "Square", "ShapeType")
      )
    );
    assertEquals(
      Direction.OUT.toString(),
      configurationFilterProvider.getTopicRouteName(
        new Session(Direction.OUT, "Square", ""),
        new TopicRoute(Direction.OUT, "Square", "ShapeType")
      )
    );
  }

  @Test
  public void testGetTopicRouteEntityName() {
    assertEquals(
      String.format(
        "%s::%s::%s",
        PropertyFactory.CONFIGURATION_DOMAIN_ROUTE_NAME,
        "Square()-IN",
        Direction.IN.toString()
      ),
      configurationFilterProvider.getTopicRouteEntityName(
        new Session(Direction.IN, "Square", ""),
        new TopicRoute(Direction.IN, "Square", "ShapeType")
      )
    );
    assertEquals(
      String.format(
        "%s::%s::%s",
        PropertyFactory.CONFIGURATION_DOMAIN_ROUTE_NAME,
        "Square()-OUT",
        Direction.OUT.toString()
      ),
      configurationFilterProvider.getTopicRouteEntityName(
        new Session(Direction.OUT, "Square", ""),
        new TopicRoute(Direction.OUT, "Square", "ShapeType")
      )
    );
  }

  @Test
  public void testGetTopicRouteConfiguration() {
    Session session = new Session(Direction.OUT, "Square", "A");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");
    String configuration = configurationFilterProvider.getTopicRouteConfiguration(session, topicRoute);

    assertTrue(configuration.contains("Square"));
    assertTrue(configuration.contains("ShapeType"));
  }
}
