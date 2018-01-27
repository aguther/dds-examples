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
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.aguther.dds.discovery.observer.PublicationObserver;
import com.github.aguther.dds.discovery.observer.SubscriptionObserver;
import com.github.aguther.dds.routing.dynamic.command.remote.DynamicPartitionCommander;
import com.github.aguther.dds.routing.dynamic.observer.DynamicPartitionObserver;
import com.github.aguther.dds.routing.dynamic.observer.DynamicPartitionObserverFilter;
import com.github.aguther.dds.routing.util.RoutingServiceCommandInterface;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.EntityNameQosPolicy;
import com.rti.dds.infrastructure.ServiceQosPolicy;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    DynamicRoutingManager.class,
    DomainParticipantQos.class,
    ServiceQosPolicy.class,
    EntityNameQosPolicy.class
})
@SuppressStaticInitializationFor({
    "com.rti.dds.domain.DomainParticipantFactory",
    "com.rti.dds.domain.DomainParticipantQos",
    "com.rti.dds.infrastructure.ServiceQosPolicy",
    "com.rti.dds.infrastructure.EntityNameQosPolicy"
})
public class DynamicRoutingManagerTest {

  private DomainParticipantFactory domainParticipantFactory;

  private DomainParticipant domainParticipantDiscovery;
  private DomainParticipant domainParticipantAdministration;
  private PublicationObserver publicationObserver;
  private SubscriptionObserver subscriptionObserver;
  private DynamicPartitionObserver dynamicPartitionObserver;
  private DynamicPartitionCommander dynamicPartitionCommander;
  private RoutingServiceCommandInterface routingServiceCommandInterface;

  private Properties properties;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    // mock the domain participant factory
    domainParticipantFactory = PowerMockito.mock(DomainParticipantFactory.class);
    Whitebox.setInternalState(DomainParticipantFactory.class, "TheParticipantFactory", domainParticipantFactory);

    // mock domain participants
    domainParticipantAdministration = mock(DomainParticipant.class);
    domainParticipantDiscovery = mock(DomainParticipant.class);

    // return mocked domain participants when factory is called
    PowerMockito.when(domainParticipantFactory.create_participant(anyInt(), any(), any(), anyInt()))
        .thenReturn(domainParticipantAdministration)
        .thenReturn(domainParticipantDiscovery);

    publicationObserver = mock(PublicationObserver.class);
    PowerMockito.whenNew(PublicationObserver.class).withAnyArguments().thenReturn(publicationObserver);

    subscriptionObserver = mock(SubscriptionObserver.class);
    PowerMockito.whenNew(SubscriptionObserver.class).withAnyArguments().thenReturn(subscriptionObserver);

    dynamicPartitionObserver = mock(DynamicPartitionObserver.class);
    PowerMockito.whenNew(DynamicPartitionObserver.class).withAnyArguments().thenReturn(dynamicPartitionObserver);

    dynamicPartitionCommander = mock(DynamicPartitionCommander.class);
    PowerMockito.whenNew(DynamicPartitionCommander.class).withAnyArguments().thenReturn(dynamicPartitionCommander);

    routingServiceCommandInterface = mock(RoutingServiceCommandInterface.class);
    {
      when(routingServiceCommandInterface.waitForDiscovery(
          anyString(), anyLong(), any(TimeUnit.class))
      ).thenReturn(true);
    }
    PowerMockito.whenNew(RoutingServiceCommandInterface.class).withAnyArguments().thenReturn(
        routingServiceCommandInterface);

    DomainParticipantQos domainParticipantQos = PowerMockito.mock(DomainParticipantQos.class);
    {
      ServiceQosPolicy serviceQosPolicy = PowerMockito.mock(ServiceQosPolicy.class);
      Whitebox.setInternalState(domainParticipantQos, "service", serviceQosPolicy);

      EntityNameQosPolicy entityNameQosPolicy = PowerMockito.mock(EntityNameQosPolicy.class);
      Whitebox.setInternalState(domainParticipantQos, "participant_name", entityNameQosPolicy);
    }
    PowerMockito.whenNew(DomainParticipantQos.class).withAnyArguments().thenReturn(domainParticipantQos);

    properties = PropertyFactory.create();

  }

  @After
  public void tearDown() {
    properties = null;
    domainParticipantFactory = null;
    domainParticipantDiscovery = null;
    domainParticipantAdministration = null;
    publicationObserver = null;
    subscriptionObserver = null;
    dynamicPartitionObserver = null;
    dynamicPartitionCommander = null;
    routingServiceCommandInterface = null;
  }

  @Test
  public void testCreate() {
    // create dynamic routing
    DynamicRoutingManager dynamicRoutingManager = new DynamicRoutingManager(
        "NAME",
        "GROUP",
        PropertyFactory.PREFIX,
        properties
    );

    // assert dynamic routing created
    assertNotNull(dynamicRoutingManager);

    // verify all close methods have been called
    verify(routingServiceCommandInterface, times(1)).waitForDiscovery(
        anyString(),
        anyLong(),
        any(TimeUnit.class)
    );
    verify(domainParticipantFactory, times(2)).create_participant(
        anyInt(),
        any(),
        any(),
        anyInt()
    );
    verify(dynamicPartitionObserver, times(3)).addFilter(
        any(DynamicPartitionObserverFilter.class)
    );
    verify(dynamicPartitionObserver, times(1)).addListener(
        dynamicPartitionCommander
    );
    verify(publicationObserver, times(1)).addListener(
        dynamicPartitionObserver,
        false
    );
    verify(subscriptionObserver, times(1)).addListener(
        dynamicPartitionObserver,
        false
    );
    verify(domainParticipantDiscovery, times(1)).enable();
  }

  @Test
  public void testCreateDiscoveryTimeout() {
    // prepare mock to timeout discovery
    when(routingServiceCommandInterface.waitForDiscovery(
        anyString(), anyLong(), any(TimeUnit.class))
    ).thenReturn(false);

    // create dynamic routing
    DynamicRoutingManager dynamicRoutingManager = new DynamicRoutingManager(
        "NAME",
        "GROUP",
        PropertyFactory.PREFIX,
        properties
    );

    // assert dynamic routing created
    assertNotNull(dynamicRoutingManager);

    // verify all close methods have been called
    verify(routingServiceCommandInterface, times(1)).waitForDiscovery(
        anyString(),
        anyLong(),
        any(TimeUnit.class)
    );
    verify(domainParticipantFactory, times(2)).create_participant(
        anyInt(),
        any(),
        any(),
        anyInt()
    );
    verify(dynamicPartitionObserver, times(3)).addFilter(
        any(DynamicPartitionObserverFilter.class)
    );
    verify(dynamicPartitionObserver, times(1)).addListener(
        dynamicPartitionCommander
    );
    verify(publicationObserver, times(1)).addListener(
        dynamicPartitionObserver,
        false
    );
    verify(subscriptionObserver, times(1)).addListener(
        dynamicPartitionObserver,
        false
    );
    verify(domainParticipantDiscovery, times(1)).enable();
  }

  @Test
  public void testClose() {
    // create dynamic routing
    DynamicRoutingManager dynamicRoutingManager = new DynamicRoutingManager(
        "NAME",
        "GROUP",
        PropertyFactory.PREFIX,
        properties
    );

    // close connection
    dynamicRoutingManager.close();

    // verify all close methods have been called
    verify(publicationObserver, times(1)).close();
    verify(subscriptionObserver, times(1)).close();
    verify(dynamicPartitionObserver, times(1)).close();
    verify(dynamicPartitionCommander, times(1)).close();
    verify(domainParticipantAdministration, times(1)).delete_contained_entities();
    verify(domainParticipantFactory, times(1)).delete_participant(domainParticipantAdministration);
    verify(domainParticipantDiscovery, times(1)).delete_contained_entities();
    verify(domainParticipantFactory, times(1)).delete_participant(domainParticipantDiscovery);
  }

  @Test
  public void testGetProperties() {
    // create dynamic routing
    DynamicRoutingManager dynamicRoutingManager = new DynamicRoutingManager(
        "NAME",
        "GROUP",
        PropertyFactory.PREFIX,
        properties
    );

    // invoke get properties
    Properties returnedProperties = dynamicRoutingManager.getProperties();

    // check if properties are equal
    assertEquals(properties, returnedProperties);
  }

  @Test
  public void testUpdate() {
    // create dynamic routing
    DynamicRoutingManager dynamicRoutingManager = new DynamicRoutingManager(
        "NAME",
        "GROUP",
        PropertyFactory.PREFIX,
        properties
    );

    // extend properties
    properties.put("NAME", "VALUE");

    // expect exception
    thrown.expect(UnsupportedOperationException.class);

    // invoke update
    dynamicRoutingManager.update(properties);
  }
}
