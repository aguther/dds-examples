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

package com.github.aguther.dds.routing.dynamic.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.aguther.dds.routing.dynamic.observer.Session;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute.Direction;
import org.junit.Before;
import org.junit.Test;

public class DynamicPartitionCommanderProviderImplTest {

  private static final String DOMAIN_ROUTE_NAME = "UnitTest";

  private DynamicPartitionCommanderProviderImpl provider;

  @Before
  public void setUp() {
    // create configuration filter
    provider = new DynamicPartitionCommanderProviderImpl(DOMAIN_ROUTE_NAME);
  }

  @Test
  public void getSessionParent() {
    assertEquals(
        DOMAIN_ROUTE_NAME,
        provider.getSessionParent(new Session("Square", "A"))
    );
  }

  @Test
  public void getSessionName() {
    assertEquals(
        "Square()",
        provider.getSessionName(new Session("Square", ""))
    );
    assertEquals(
        "Square(A)",
        provider.getSessionName(new Session("Square", "A"))
    );
  }

  @Test
  public void getSessionEntityName() {
    assertEquals(
        String.format(
            "%s::%s",
            DOMAIN_ROUTE_NAME,
            "Square()"
        ),
        provider.getSessionEntityName(new Session("Square", ""))
    );
    assertEquals(
        String.format(
            "%s::%s",
            DOMAIN_ROUTE_NAME,
            "Square(A)"
        ),
        provider.getSessionEntityName(new Session("Square", "A"))
    );
  }

  @Test
  public void getSessionConfiguration() {
    Session session = new Session("Square", "A");
    String configuration = provider.getSessionConfiguration(session);

    assertTrue(configuration.contains("Square"));
    assertTrue(configuration.contains("A"));
  }

  @Test
  public void getTopicRouteName() {
    assertEquals(
        Direction.IN.toString(),
        provider.getTopicRouteName(
            new Session("Square", ""),
            new TopicRoute(Direction.IN, "Square", "ShapeType")
        )
    );
    assertEquals(
        Direction.OUT.toString(),
        provider.getTopicRouteName(
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
            DOMAIN_ROUTE_NAME,
            "Square()",
            Direction.IN.toString()
        ),
        provider.getTopicRouteEntityName(
            new Session("Square", ""),
            new TopicRoute(Direction.IN, "Square", "ShapeType")
        )
    );
    assertEquals(
        String.format(
            "%s::%s::%s",
            DOMAIN_ROUTE_NAME,
            "Square()",
            Direction.OUT.toString()
        ),
        provider.getTopicRouteEntityName(
            new Session("Square", ""),
            new TopicRoute(Direction.OUT, "Square", "ShapeType")
        )
    );
  }

  @Test
  public void getTopicRouteConfiguration() {
    Session session = new Session("Square", "A");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");
    String configuration = provider.getTopicRouteConfiguration(session, topicRoute);

    assertTrue(configuration.contains("Square"));
    assertFalse(configuration.contains("ShapeType"));
  }
}
