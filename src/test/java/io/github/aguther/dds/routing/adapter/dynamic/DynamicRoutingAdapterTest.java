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

package io.github.aguther.dds.routing.adapter.dynamic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.aguther.dds.routing.dynamic.PropertyFactory;
import com.rti.dds.infrastructure.RETCODE_ERROR;
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
@PrepareForTest(DynamicRoutingAdapter.class)
public class DynamicRoutingAdapterTest {

  private static final String ROUTING_SERVICE_NAME = "UnitTest";
  private static final String ROUTING_SERVICE_GROUP_NAME = "UnitTestGroup";

  private Properties properties;
  private DynamicRoutingAdapter dynamicRoutingAdapter;

  private DynamicRoutingConnection dynamicRoutingConnection;

  @Before
  public void setUp() throws Exception {
    properties = PropertyFactory.create();
    dynamicRoutingAdapter = new DynamicRoutingAdapter(
        properties
    );

    dynamicRoutingConnection = mock(DynamicRoutingConnection.class);
    PowerMockito.whenNew(DynamicRoutingConnection.class).withAnyArguments().thenReturn(null);
    PowerMockito.whenNew(DynamicRoutingConnection.class).withArguments(
        ROUTING_SERVICE_NAME,
        ROUTING_SERVICE_GROUP_NAME,
        properties
    ).thenReturn(dynamicRoutingConnection);
  }

  @After
  public void tearDown() {
    dynamicRoutingAdapter = null;
    dynamicRoutingConnection = null;
  }


  @Test
  public void testCreateConnection() throws AdapterException {
    // create connection
    DynamicRoutingConnection connection = (DynamicRoutingConnection) dynamicRoutingAdapter.createConnection(
        ROUTING_SERVICE_NAME,
        ROUTING_SERVICE_GROUP_NAME,
        null,
        null,
        properties
    );

    // ensure we got the mock (-> parameters have been correctly forwarded, otherwise we will get null)
    assertEquals(dynamicRoutingConnection, connection);
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testCreateConnectionException() throws Exception {
    // throw exception when connection is created
    PowerMockito.whenNew(DynamicRoutingConnection.class).withAnyArguments().thenThrow(new RETCODE_ERROR());

    // exception of type AdapterException is expected
    thrown.expect(AdapterException.class);

    // create connection
    DynamicRoutingConnection connection = (DynamicRoutingConnection) dynamicRoutingAdapter.createConnection(
        ROUTING_SERVICE_NAME,
        ROUTING_SERVICE_GROUP_NAME,
        null,
        null,
        properties
    );
  }

  @Test
  public void testDeleteConnection() throws AdapterException {
    dynamicRoutingAdapter.deleteConnection(dynamicRoutingConnection);
    verify(dynamicRoutingConnection, times(1)).close();
  }

  @Test
  public void testDeleteConnectionException() throws AdapterException {
    // throw exception when connection is closed
    doThrow(new RETCODE_ERROR()).when(dynamicRoutingConnection).close();

    // exception of type AdapterException is expected
    thrown.expect(AdapterException.class);

    // close connection
    dynamicRoutingAdapter.deleteConnection(dynamicRoutingConnection);
  }

  @Test
  public void testGetVersion() {
    assertNotNull(dynamicRoutingAdapter.getVersion());
  }
}
