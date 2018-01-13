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

package com.github.aguther.dds.routing.dynamic.observer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.aguther.dds.routing.dynamic.observer.TopicRoute.Direction;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.PartitionQosPolicy;
import com.rti.dds.infrastructure.StringSeq;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    DynamicPartitionObserver.class,
    LoggerFactory.class,
    PartitionQosPolicy.class,
    PublicationBuiltinTopicData.class,
    SubscriptionBuiltinTopicData.class,
})
public class DynamicPartitionObserverTest {

  private static final int VERIFY_TIMEOUT = 5000;

  private DomainParticipant domainParticipant;

  private DynamicPartitionObserver observer;
  private DynamicPartitionObserverFilter filter;
  private DynamicPartitionObserverListener listener;

  @Before
  public void setUp() {
    domainParticipant = mock(DomainParticipant.class);

    filter = mock(DynamicPartitionObserverFilter.class);
    listener = mock(DynamicPartitionObserverListener.class);

    observer = new DynamicPartitionObserver();
    observer.addFilter(filter);
    observer.addListener(listener);
  }

  @After
  public void tearDown() {
    observer.removeListener(listener);
    observer.removeFilter(filter);
    observer.close();
  }

  @Test
  public void testIgnorePublication() {
    Session session = new Session("Square", "");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");

    InstanceHandle_t instanceHandle = createInstanceHandle(0);
    PublicationBuiltinTopicData publicationBuiltinTopicData = createPublicationBuiltinTopicData(
        session.getTopic(),
        topicRoute.getType()
    );

    when(filter.ignorePublication(domainParticipant, instanceHandle, publicationBuiltinTopicData)).thenReturn(true);

    observer.publicationDiscovered(
        domainParticipant,
        instanceHandle,
        publicationBuiltinTopicData
    );
    observer.publicationLost(
        domainParticipant,
        instanceHandle,
        publicationBuiltinTopicData
    );

    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .createSession(any(Session.class));
    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .deleteSession(any(Session.class));

    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .createTopicRoute(any(Session.class), any(TopicRoute.class));
    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .deleteTopicRoute(any(Session.class), any(TopicRoute.class));
  }

  @Test
  public void testIgnoreSubscription() {
    Session session = new Session("Square", "");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");

    InstanceHandle_t instanceHandle = createInstanceHandle(0);
    SubscriptionBuiltinTopicData subscriptionBuiltinTopicData = createSubscriptionBuiltinTopicData(
        session.getTopic(),
        topicRoute.getType()
    );

    when(filter.ignoreSubscription(domainParticipant, instanceHandle, subscriptionBuiltinTopicData)).thenReturn(true);

    observer.subscriptionDiscovered(
        domainParticipant,
        instanceHandle,
        subscriptionBuiltinTopicData
    );
    observer.subscriptionLost(
        domainParticipant,
        instanceHandle,
        subscriptionBuiltinTopicData
    );

    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .createSession(any(Session.class));
    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .deleteSession(any(Session.class));

    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .createTopicRoute(any(Session.class), any(TopicRoute.class));
    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .deleteTopicRoute(any(Session.class), any(TopicRoute.class));
  }

  @Test
  public void testIgnoreDefaultPartition() {
    Session session = new Session("Square", "");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");

    InstanceHandle_t instanceHandle = createInstanceHandle(0);
    PublicationBuiltinTopicData publicationBuiltinTopicData = createPublicationBuiltinTopicData(
        session.getTopic(),
        topicRoute.getType()
    );
    SubscriptionBuiltinTopicData subscriptionBuiltinTopicData = createSubscriptionBuiltinTopicData(
        session.getTopic(),
        topicRoute.getType()
    );

    when(filter.ignorePartition("Square", "")).thenReturn(true);

    observer.publicationDiscovered(
        domainParticipant,
        instanceHandle,
        publicationBuiltinTopicData
    );
    observer.publicationLost(
        domainParticipant,
        instanceHandle,
        publicationBuiltinTopicData
    );

    observer.subscriptionDiscovered(
        domainParticipant,
        instanceHandle,
        subscriptionBuiltinTopicData
    );
    observer.subscriptionLost(
        domainParticipant,
        instanceHandle,
        subscriptionBuiltinTopicData
    );

    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .createSession(any(Session.class));
    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .deleteSession(any(Session.class));

    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .createTopicRoute(any(Session.class), any(TopicRoute.class));
    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .deleteTopicRoute(any(Session.class), any(TopicRoute.class));
  }

  @Test
  public void testIgnorePartition() {
    Session session = new Session("Square", "A");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");

    InstanceHandle_t instanceHandle = createInstanceHandle(0);
    PublicationBuiltinTopicData publicationBuiltinTopicData = createPublicationBuiltinTopicData(
        session.getTopic(),
        topicRoute.getType(),
        session.getPartition()
    );
    SubscriptionBuiltinTopicData subscriptionBuiltinTopicData = createSubscriptionBuiltinTopicData(
        session.getTopic(),
        topicRoute.getType(),
        session.getPartition()
    );

    when(filter.ignorePartition(session.getTopic(), session.getPartition())).thenReturn(true);

    observer.publicationDiscovered(
        domainParticipant,
        instanceHandle,
        publicationBuiltinTopicData
    );
    observer.publicationLost(
        domainParticipant,
        instanceHandle,
        publicationBuiltinTopicData
    );

    observer.subscriptionDiscovered(
        domainParticipant,
        instanceHandle,
        subscriptionBuiltinTopicData
    );
    observer.subscriptionLost(
        domainParticipant,
        instanceHandle,
        subscriptionBuiltinTopicData
    );

    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .createSession(any(Session.class));
    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .deleteSession(any(Session.class));

    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .createTopicRoute(any(Session.class), any(TopicRoute.class));
    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .deleteTopicRoute(any(Session.class), any(TopicRoute.class));
  }

  @Test
  public void testSessionWithOneTopicRoute() {
    Session session = new Session("Square", "A");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");

    InstanceHandle_t instanceHandle = createInstanceHandle(0);
    PublicationBuiltinTopicData publicationBuiltinTopicData = createPublicationBuiltinTopicData(
        session.getTopic(),
        topicRoute.getType(),
        session.getPartition()
    );

    observer.publicationDiscovered(
        domainParticipant,
        instanceHandle,
        publicationBuiltinTopicData
    );

    observer.publicationLost(
        domainParticipant,
        instanceHandle,
        publicationBuiltinTopicData
    );

    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .createSession(session);
    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .deleteSession(session);

    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .createTopicRoute(session, topicRoute);
    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .deleteTopicRoute(session, topicRoute);
  }

  @Test
  public void testSessionWithTwoTopicRoutes() {
    Session session = new Session("Square", "A");
    TopicRoute topicRouteOut = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");
    TopicRoute topicRouteIn = new TopicRoute(Direction.IN, topicRouteOut.getTopic(), topicRouteOut.getType());

    InstanceHandle_t instanceHandlePublication = createInstanceHandle(0);
    PublicationBuiltinTopicData publicationBuiltinTopicData = createPublicationBuiltinTopicData(
        session.getTopic(),
        topicRouteOut.getType(),
        session.getPartition()
    );

    observer.publicationDiscovered(
        domainParticipant,
        instanceHandlePublication,
        publicationBuiltinTopicData
    );

    InstanceHandle_t instanceHandleSubscription = createInstanceHandle(1);
    SubscriptionBuiltinTopicData subscriptionBuiltinTopicData = createSubscriptionBuiltinTopicData(
        session.getTopic(),
        topicRouteIn.getType(),
        session.getPartition()
    );

    observer.subscriptionDiscovered(
        domainParticipant,
        instanceHandleSubscription,
        subscriptionBuiltinTopicData
    );

    observer.subscriptionLost(
        domainParticipant,
        instanceHandleSubscription,
        subscriptionBuiltinTopicData
    );
    observer.publicationLost(
        domainParticipant,
        instanceHandlePublication,
        publicationBuiltinTopicData
    );

    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .createSession(session);
    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .deleteSession(session);

    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .createTopicRoute(session, topicRouteOut);
    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .deleteTopicRoute(session, topicRouteOut);

    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .createTopicRoute(session, topicRouteIn);
    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .deleteTopicRoute(session, topicRouteIn);
  }

  @Test
  public void testSessionWithOneTopicRouteTwoPublications() {
    Session session = new Session("Square", "A");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");

    InstanceHandle_t instanceHandle1 = createInstanceHandle(1);
    PublicationBuiltinTopicData publicationBuiltinTopicData1 = createPublicationBuiltinTopicData(
        session.getTopic(),
        topicRoute.getType(),
        session.getPartition()
    );

    observer.publicationDiscovered(
        domainParticipant,
        instanceHandle1,
        publicationBuiltinTopicData1
    );

    InstanceHandle_t instanceHandle2 = createInstanceHandle(2);
    PublicationBuiltinTopicData publicationBuiltinTopicData2 = createPublicationBuiltinTopicData(
        session.getTopic(),
        topicRoute.getType(),
        session.getPartition()
    );

    observer.publicationDiscovered(
        domainParticipant,
        instanceHandle2,
        publicationBuiltinTopicData2
    );

    observer.publicationLost(
        domainParticipant,
        instanceHandle2,
        publicationBuiltinTopicData2
    );
    observer.publicationLost(
        domainParticipant,
        instanceHandle1,
        publicationBuiltinTopicData1
    );

    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .createSession(session);
    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .deleteSession(session);

    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .createTopicRoute(session, topicRoute);
    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .deleteTopicRoute(session, topicRoute);
  }

  @Test
  public void testSessionWithOneTopicRouteTwoPublicationsDefaultPartition() {
    Session session = new Session("Square", "");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");

    InstanceHandle_t instanceHandle1 = createInstanceHandle(1);
    PublicationBuiltinTopicData publicationBuiltinTopicData1 = createPublicationBuiltinTopicData(
        session.getTopic(),
        topicRoute.getType()
    );

    observer.publicationDiscovered(
        domainParticipant,
        instanceHandle1,
        publicationBuiltinTopicData1
    );

    InstanceHandle_t instanceHandle2 = createInstanceHandle(2);
    PublicationBuiltinTopicData publicationBuiltinTopicData2 = createPublicationBuiltinTopicData(
        session.getTopic(),
        topicRoute.getType()
    );

    observer.publicationDiscovered(
        domainParticipant,
        instanceHandle2,
        publicationBuiltinTopicData2
    );

    observer.publicationLost(
        domainParticipant,
        instanceHandle2,
        publicationBuiltinTopicData2
    );
    observer.publicationLost(
        domainParticipant,
        instanceHandle1,
        publicationBuiltinTopicData1
    );

    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .createSession(session);
    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .deleteSession(session);

    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .createTopicRoute(session, topicRoute);
    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .deleteTopicRoute(session, topicRoute);
  }

  @Test
  public void testPublicationWithTwoPartitions() {
    Session sessionA = new Session("Square", "A");
    Session sessionB = new Session(sessionA.getTopic(), "B");
    TopicRoute topicRouteA = new TopicRoute(Direction.OUT, sessionA.getTopic(), "ShapeType");
    TopicRoute topicRouteB = new TopicRoute(Direction.OUT, sessionB.getTopic(), topicRouteA.getType());

    InstanceHandle_t instanceHandle = createInstanceHandle(0);
    PublicationBuiltinTopicData publicationBuiltinTopicData = createPublicationBuiltinTopicData(
        sessionA.getTopic(),
        topicRouteA.getType(),
        sessionA.getPartition(),
        sessionB.getPartition()
    );

    observer.publicationDiscovered(
        domainParticipant,
        instanceHandle,
        publicationBuiltinTopicData
    );

    observer.publicationLost(
        domainParticipant,
        instanceHandle,
        publicationBuiltinTopicData
    );

    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .createSession(sessionA);
    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .deleteSession(sessionA);

    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .createSession(sessionB);
    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .deleteSession(sessionB);

    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .createTopicRoute(sessionA, topicRouteA);
    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .deleteTopicRoute(sessionA, topicRouteA);

    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .createTopicRoute(sessionB, topicRouteB);
    verify(listener, timeout(VERIFY_TIMEOUT).times(1))
        .deleteTopicRoute(sessionB, topicRouteB);
  }

  private InstanceHandle_t createInstanceHandle(
      int identification
  ) {
    return new InstanceHandle_t(ByteBuffer.allocate(4).putInt(identification).array());
  }

  private PublicationBuiltinTopicData createPublicationBuiltinTopicData(
      String topic,
      String type,
      String... partitions
  ) {
    PublicationBuiltinTopicData builtinTopicData = mock(PublicationBuiltinTopicData.class);
    PartitionQosPolicy partitionQosPolicy = mock(PartitionQosPolicy.class);
    Whitebox.setInternalState(builtinTopicData, "partition", partitionQosPolicy);
    Whitebox.setInternalState(partitionQosPolicy, "name", new StringSeq());

    builtinTopicData.topic_name = topic;
    builtinTopicData.type_name = type;
    builtinTopicData.partition.name.addAll(Arrays.asList(partitions));
    return builtinTopicData;
  }

  private SubscriptionBuiltinTopicData createSubscriptionBuiltinTopicData(
      String topic,
      String type,
      String... partitions
  ) {
    SubscriptionBuiltinTopicData builtinTopicData = mock(SubscriptionBuiltinTopicData.class);
    PartitionQosPolicy partitionQosPolicy = mock(PartitionQosPolicy.class);
    Whitebox.setInternalState(builtinTopicData, "partition", partitionQosPolicy);
    Whitebox.setInternalState(partitionQosPolicy, "name", new StringSeq());

    builtinTopicData.topic_name = topic;
    builtinTopicData.type_name = type;
    builtinTopicData.partition.name.addAll(Arrays.asList(partitions));

    return builtinTopicData;
  }

  @Test
  public void testLostNonExistingSession() {
    Session session = new Session("Square", "A");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");

    InstanceHandle_t instanceHandle = createInstanceHandle(0);
    PublicationBuiltinTopicData publicationBuiltinTopicData = createPublicationBuiltinTopicData(
        session.getTopic(),
        topicRoute.getType(),
        session.getPartition()
    );

    observer.publicationLost(
        domainParticipant,
        instanceHandle,
        publicationBuiltinTopicData
    );

    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .createSession(session);
    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .deleteSession(session);

    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .createTopicRoute(session, topicRoute);
    verify(listener, timeout(VERIFY_TIMEOUT).times(0))
        .deleteTopicRoute(session, topicRoute);
  }
}
