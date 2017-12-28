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

import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DynamicRoutingAdapterTest {

  private static final String ROUTING_SERVICE_NAME = "UnitTest";
  private static final String ROUTING_SERVICE_GROUP_NAME = "UnitTestGroup";

  private Properties properties;
  private DynamicRoutingAdapter dynamicRoutingAdapter;

  @Before
  public void setUp() throws Exception {
    properties = PropertyFactory.create();
    dynamicRoutingAdapter = new DynamicRoutingAdapter(
        properties
    );
  }

  @After
  public void tearDown() {
    dynamicRoutingAdapter = null;
  }

  @Test
  public void testGetVersion() {
    assertNotNull(dynamicRoutingAdapter.getVersion());
  }
}
