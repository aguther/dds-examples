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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.aguther.dds.routing.dynamic.observer.Session;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute.Direction;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import org.junit.Before;
import org.junit.Test;

public class ConfigurationFilterTest {

  private ConfigurationFilter configurationFilter;

  @Before
  public void setUp() throws Exception {
    // create configuration filter
    configurationFilter = new ConfigurationFilter(PropertyFactory.create());
  }

  @Test
  public void ignorePublication() {
    PublicationBuiltinTopicData publicationBuiltinTopicData = new PublicationBuiltinTopicData();

    publicationBuiltinTopicData.topic_name = "Square";
    assertFalse(configurationFilter.ignorePublication(null, null, publicationBuiltinTopicData));
    publicationBuiltinTopicData.topic_name = "Triangle";
    assertFalse(configurationFilter.ignorePublication(null, null, publicationBuiltinTopicData));

    publicationBuiltinTopicData.topic_name = "Circle";
    assertTrue(configurationFilter.ignorePublication(null, null, publicationBuiltinTopicData));
    publicationBuiltinTopicData.topic_name = "OtherTopic";
    assertTrue(configurationFilter.ignorePublication(null, null, publicationBuiltinTopicData));
  }

  @Test
  public void ignoreSubscription() {
    SubscriptionBuiltinTopicData subscriptionBuiltinTopicData = new SubscriptionBuiltinTopicData();

    subscriptionBuiltinTopicData.topic_name = "Square";
    assertFalse(configurationFilter.ignoreSubscription(null, null, subscriptionBuiltinTopicData));
    subscriptionBuiltinTopicData.topic_name = "Triangle";
    assertFalse(configurationFilter.ignoreSubscription(null, null, subscriptionBuiltinTopicData));

    subscriptionBuiltinTopicData.topic_name = "Circle";
    assertTrue(configurationFilter.ignoreSubscription(null, null, subscriptionBuiltinTopicData));
  }

  @Test
  public void ignorePartition() {
    assertFalse(configurationFilter.ignorePartition("A"));
    assertFalse(configurationFilter.ignorePartition("ATest"));
    assertFalse(configurationFilter.ignorePartition("B"));

    assertTrue(configurationFilter.ignorePartition("*"));
    assertTrue(configurationFilter.ignorePartition("A*"));
    assertTrue(configurationFilter.ignorePartition("D"));
    assertTrue(configurationFilter.ignorePartition("D*"));

    assertTrue(configurationFilter.ignorePartition("C"));
  }

  @Test
  public void getSessionParent() {
    assertEquals(
        PropertyFactory.CONFIGURATION_DOMAIN_ROUTE_NAME,
        configurationFilter.getSessionParent(new Session("Square", "A"))
    );
  }

  @Test
  public void getSessionName() {
    assertEquals(
        "Square()",
        configurationFilter.getSessionName(new Session("Square", ""))
    );
    assertEquals(
        "Square(A)",
        configurationFilter.getSessionName(new Session("Square", "A"))
    );
  }

  @Test
  public void getSessionEntityName() {
    assertEquals(
        String.format(
            "%s::%s",
            PropertyFactory.CONFIGURATION_DOMAIN_ROUTE_NAME,
            "Square()"
        ),
        configurationFilter.getSessionEntityName(new Session("Square", ""))
    );
    assertEquals(
        String.format(
            "%s::%s",
            PropertyFactory.CONFIGURATION_DOMAIN_ROUTE_NAME,
            "Square(A)"
        ),
        configurationFilter.getSessionEntityName(new Session("Square", "A"))
    );
  }

  @Test
  public void getSessionConfiguration() {
    Session session = new Session("Square", "A");
    String configuration = configurationFilter.getSessionConfiguration(session);

    assertTrue(configuration.contains("Square"));
    assertTrue(configuration.contains("A"));
  }

  @Test
  public void getTopicRouteName() {
    assertEquals(
        Direction.IN.toString(),
        configurationFilter.getTopicRouteName(
            new Session("Square", ""),
            new TopicRoute(Direction.IN, "Square", "ShapeType")
        )
    );
    assertEquals(
        Direction.OUT.toString(),
        configurationFilter.getTopicRouteName(
            new Session("Square", ""),
            new TopicRoute(Direction.OUT, "Square", "ShapeType")
        )
    );
  }

  @Test
  public void getTopicRouteEntityName() {
    assertEquals(
        String.format(
            "%s::%s::%s",
            PropertyFactory.CONFIGURATION_DOMAIN_ROUTE_NAME,
            "Square()",
            Direction.IN.toString()
        ),
        configurationFilter.getTopicRouteEntityName(
            new Session("Square", ""),
            new TopicRoute(Direction.IN, "Square", "ShapeType")
        )
    );
    assertEquals(
        String.format(
            "%s::%s::%s",
            PropertyFactory.CONFIGURATION_DOMAIN_ROUTE_NAME,
            "Square()",
            Direction.OUT.toString()
        ),
        configurationFilter.getTopicRouteEntityName(
            new Session("Square", ""),
            new TopicRoute(Direction.OUT, "Square", "ShapeType")
        )
    );
  }

  @Test
  public void getTopicRouteConfiguration() {
    Session session = new Session("Square", "A");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");
    String configuration = configurationFilter.getTopicRouteConfiguration(session, topicRoute);

    assertTrue(configuration.contains("Square"));
    assertTrue(configuration.contains("ShapeType"));
  }
}