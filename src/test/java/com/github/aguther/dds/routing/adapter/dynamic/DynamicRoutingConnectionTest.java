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
import com.github.aguther.dds.routing.dynamic.DynamicRoutingManager;
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
  private DynamicRoutingManager dynamicRoutingManager;
  private DynamicRoutingConnection dynamicRoutingConnection;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    // create properties
    properties = PropertyFactory.create();

    // mock dynamic routing
    dynamicRoutingManager = mock(DynamicRoutingManager.class);
    {
      when(dynamicRoutingManager.getProperties()).thenReturn(properties);
    }
    PowerMockito.whenNew(DynamicRoutingManager.class).withAnyArguments().thenReturn(dynamicRoutingManager);

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
    dynamicRoutingManager = null;
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
    verify(dynamicRoutingManager, times(1)).close();
  }

  @Test
  public void testUpdate() throws AdapterException {
    // extend properties
    properties.put("NAME", "VALUE");

    // get attributes
    dynamicRoutingConnection.update(properties);

    // verify update has been called
    verify(dynamicRoutingManager, times(1)).update(properties);
  }

  @Test
  public void testGetAttributes() throws AdapterException {
    // get attributes
    assertEquals(properties, dynamicRoutingConnection.getAttributes());
  }

  @Test
  public void testCreateSession() throws AdapterException {
    assertTrue(dynamicRoutingConnection.createSession(properties) instanceof EmptySession);
    verifyZeroInteractions(dynamicRoutingManager);
  }

  @Test
  public void testDeleteSession() throws AdapterException {
    dynamicRoutingConnection.deleteSession(new EmptySession());
    verifyZeroInteractions(dynamicRoutingManager);
  }

  @Test
  public void testCreateStreamReader() throws AdapterException {
    assertTrue(dynamicRoutingConnection.createStreamReader(
      null, null, properties, null) instanceof EmptyStreamReader);
    verifyZeroInteractions(dynamicRoutingManager);
  }

  @Test
  public void testDeleteStreamReader() throws AdapterException {
    dynamicRoutingConnection.deleteStreamReader(new EmptyStreamReader());
    verifyZeroInteractions(dynamicRoutingManager);
  }

  @Test
  public void testCreateStreamWriter() throws AdapterException {
    assertTrue(dynamicRoutingConnection.createStreamWriter(
      null, null, properties) instanceof EmptyStreamWriter);
    verifyZeroInteractions(dynamicRoutingManager);
  }

  @Test
  public void testDeleteStreamWriter() throws AdapterException {
    dynamicRoutingConnection.deleteStreamWriter(new EmptyStreamWriter());
    verifyZeroInteractions(dynamicRoutingManager);
  }

  @Test
  public void testGetInputStreamDiscoveryReader() throws AdapterException {
    assertTrue(dynamicRoutingConnection.getInputStreamDiscoveryReader() instanceof EmptyStreamReader);
    verifyZeroInteractions(dynamicRoutingManager);
  }

  @Test
  public void testGetOutputStreamDiscoveryReader() throws AdapterException {
    assertTrue(dynamicRoutingConnection.getOutputStreamDiscoveryReader() instanceof EmptyStreamReader);
    verifyZeroInteractions(dynamicRoutingManager);
  }

  @Test
  public void testCopyTypeRepresentation() throws AdapterException {
    thrown.expect(AdapterException.class);
    dynamicRoutingConnection.copyTypeRepresentation(new Object());
    verifyZeroInteractions(dynamicRoutingManager);
  }

  @Test
  public void testDeleteTypeRepresentation() throws AdapterException {
    dynamicRoutingConnection.deleteTypeRepresentation(new Object());
    verifyZeroInteractions(dynamicRoutingManager);
  }
}
