package com.github.aguther.dds.routing.adapter.dynamic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.github.aguther.dds.discovery.observer.PublicationObserver;
import com.github.aguther.dds.discovery.observer.SubscriptionObserver;
import com.github.aguther.dds.routing.dynamic.command.DynamicPartitionCommander;
import com.github.aguther.dds.routing.dynamic.observer.DynamicPartitionObserver;
import com.github.aguther.dds.routing.dynamic.observer.DynamicPartitionObserverFilter;
import com.github.aguther.dds.routing.util.RoutingServiceCommandInterface;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.EntityNameQosPolicy;
import com.rti.dds.infrastructure.ServiceQosPolicy;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DynamicRoutingConnection.class, DomainParticipantQos.class, ServiceQosPolicy.class,
    EntityNameQosPolicy.class})
@SuppressStaticInitializationFor("com.rti.dds.domain.DomainParticipantFactory")
public class DynamicRoutingConnectionTest {

  private DomainParticipantFactory domainParticipantFactory;

  private DomainParticipant domainParticipantDiscovery;
  private DomainParticipant domainParticipantAdministration;
  private PublicationObserver publicationObserver;
  private SubscriptionObserver subscriptionObserver;
  private DynamicPartitionObserver dynamicPartitionObserver;
  private DynamicPartitionCommander dynamicPartitionCommander;
  private RoutingServiceCommandInterface routingServiceCommandInterface;

  private DomainParticipantQos domainParticipantQos;
//  private ServiceQosPolicy serviceQosPolicy;
//  private EntityNameQosPolicy participant_name;

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
    PowerMockito.whenNew(RoutingServiceCommandInterface.class).withAnyArguments().thenReturn(
        routingServiceCommandInterface);

    ServiceQosPolicy serviceQosPolicy = PowerMockito.mock(ServiceQosPolicy.class);
    EntityNameQosPolicy entityNameQosPolicy = PowerMockito.mock(EntityNameQosPolicy.class);
    domainParticipantQos = PowerMockito.mock(DomainParticipantQos.class);
    Whitebox.setInternalState(domainParticipantQos, "service", serviceQosPolicy);
    Whitebox.setInternalState(domainParticipantQos, "participant_name", entityNameQosPolicy);
    PowerMockito.whenNew(DomainParticipantQos.class).withAnyArguments().thenReturn(domainParticipantQos);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testCreate() {
    // create connection
    DynamicRoutingConnection connection = new DynamicRoutingConnection(
        "NAME",
        "GROUP",
        PropertyFactory.create()
    );

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
    // create connection
    DynamicRoutingConnection connection = new DynamicRoutingConnection(
        "NAME",
        "GROUP",
        PropertyFactory.create()
    );

    // close connection
    connection.close();

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
}
