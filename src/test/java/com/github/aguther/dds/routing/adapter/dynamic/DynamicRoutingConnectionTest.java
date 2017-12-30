package com.github.aguther.dds.routing.adapter.dynamic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.github.aguther.dds.routing.adapter.empty.EmptySession;
import com.github.aguther.dds.routing.adapter.empty.EmptyStreamReader;
import com.github.aguther.dds.routing.adapter.empty.EmptyStreamWriter;
import com.github.aguther.dds.routing.dynamic.DynamicRouting;
import com.github.aguther.dds.routing.dynamic.PropertyFactory;
import com.rti.routingservice.adapter.infrastructure.AdapterException;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DynamicRoutingConnection.class)
public class DynamicRoutingConnectionTest {

  private Properties properties;
  private DynamicRouting dynamicRouting;
  private DynamicRoutingConnection dynamicRoutingConnection;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    // create properties
    properties = PropertyFactory.create();

    // mock dynamic routing
    dynamicRouting = mock(DynamicRouting.class);
    {
      when(dynamicRouting.getProperties()).thenReturn(properties);
    }
    PowerMockito.whenNew(DynamicRouting.class).withAnyArguments().thenReturn(dynamicRouting);

    // create connection
    dynamicRoutingConnection = new DynamicRoutingConnection(
        "NAME",
        "GROUP",
        properties
    );
  }

  @After
  public void tearDown() throws Exception {
    properties = null;
    dynamicRouting = null;
    dynamicRoutingConnection = null;
  }

  @Test
  public void testCreate() {
    assertNotNull(dynamicRoutingConnection);
  }

  @Test
  public void testClose() {
    // close connection
    dynamicRoutingConnection.close();

    // verify all close methods have been called
    verify(dynamicRouting, times(1)).close();
  }

  @Test
  public void testUpdate() throws AdapterException {
    // extend properties
    properties.put("NAME", "VALUE");

    // get attributes
    dynamicRoutingConnection.update(properties);

    // verify update has been called
    verify(dynamicRouting, times(1)).update(properties);
  }

  @Test
  public void testGetAttributes() throws AdapterException {
    // get attributes
    assertEquals(properties, dynamicRoutingConnection.getAttributes());
  }

  @Test
  public void testCreateSession() throws AdapterException {
    assertTrue(dynamicRoutingConnection.createSession(properties) instanceof EmptySession);
    verifyZeroInteractions(dynamicRouting);
  }

  @Test
  public void testDeleteSession() throws AdapterException {
    dynamicRoutingConnection.deleteSession(new EmptySession());
    verifyZeroInteractions(dynamicRouting);
  }

  @Test
  public void testCreateStreamReader() throws AdapterException {
    assertTrue(dynamicRoutingConnection.createStreamReader(
        null, null, properties, null) instanceof EmptyStreamReader);
    verifyZeroInteractions(dynamicRouting);
  }

  @Test
  public void testDeleteStreamReader() throws AdapterException {
    dynamicRoutingConnection.deleteStreamReader(new EmptyStreamReader());
    verifyZeroInteractions(dynamicRouting);
  }

  @Test
  public void testCreateStreamWriter() throws AdapterException {
    assertTrue(dynamicRoutingConnection.createStreamWriter(
        null, null, properties) instanceof EmptyStreamWriter);
    verifyZeroInteractions(dynamicRouting);
  }

  @Test
  public void testDeleteStreamWriter() throws AdapterException {
    dynamicRoutingConnection.deleteStreamWriter(new EmptyStreamWriter());
    verifyZeroInteractions(dynamicRouting);
  }

  @Test
  public void testGetInputStreamDiscoveryReader() throws AdapterException {
    assertTrue(dynamicRoutingConnection.getInputStreamDiscoveryReader() instanceof EmptyStreamReader);
    verifyZeroInteractions(dynamicRouting);
  }

  @Test
  public void testGetOutputStreamDiscoveryReader() throws AdapterException {
    assertTrue(dynamicRoutingConnection.getOutputStreamDiscoveryReader() instanceof EmptyStreamReader);
    verifyZeroInteractions(dynamicRouting);
  }

  @Test
  public void testCopyTypeRepresentation() throws AdapterException {
    thrown.expect(AdapterException.class);
    dynamicRoutingConnection.copyTypeRepresentation(new Object());
    verifyZeroInteractions(dynamicRouting);
  }

  @Test
  public void testDeleteTypeRepresentation() throws AdapterException {
    dynamicRoutingConnection.deleteTypeRepresentation(new Object());
    verifyZeroInteractions(dynamicRouting);
  }
}
