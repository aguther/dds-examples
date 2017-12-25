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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.rti.routingservice.adapter.infrastructure.AdapterException;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LoggerFactory.class, DynamicRoutingAdapterTest.class})
public class DynamicRoutingAdapterTest {

  private static final String ROUTING_SERVICE_NAME = "UnitTest";
  private static final String ROUTING_SERVICE_GROUP_NAME = "UnitTestGroup";

  private Properties properties;
  private DynamicRoutingAdapter dynamicRoutingAdapter;

  @Before
  public void setUp() throws Exception {
    Logger logger = mock(Logger.class);
    mockStatic(LoggerFactory.class);
    when(LoggerFactory.getLogger(DynamicRoutingAdapter.class)).thenReturn(logger);
    when(logger.isDebugEnabled()).thenReturn(true);

    properties = PropertyFactory.create();

    dynamicRoutingAdapter = new DynamicRoutingAdapter(
        properties
    );
  }

  @After
  public void tearDown() {
    dynamicRoutingAdapter = null;
  }

  @Ignore
  @Test
  public void testCreateConnection() throws AdapterException {
    dynamicRoutingAdapter.createConnection(
        "RoutingServiceName",
        "RoutingServiceGroupName",
        null,
        null,
        properties
    );
  }

  @Ignore
  @Test
  public void testDeleteConnection() {

  }

  @Test
  public void testGetVersion() {
    assertNotNull(dynamicRoutingAdapter.getVersion());
  }
}
