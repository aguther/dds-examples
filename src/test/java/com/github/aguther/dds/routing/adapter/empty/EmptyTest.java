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

package com.github.aguther.dds.routing.adapter.empty;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.rti.routingservice.adapter.Connection;
import com.rti.routingservice.adapter.Session;
import com.rti.routingservice.adapter.StreamReader;
import com.rti.routingservice.adapter.StreamWriter;
import com.rti.routingservice.adapter.infrastructure.AdapterException;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EmptyTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testAdapter() throws AdapterException {
    Properties properties = new Properties();

    EmptyAdapter adapter = new EmptyAdapter(
        properties
    );
    assertNotNull(adapter);
    assertNotNull(adapter.getVersion());

    Connection connection = adapter.createConnection(
        "Name",
        "Group",
        null,
        null,
        properties
    );
    assertTrue(connection instanceof EmptyConnection);
    EmptyConnection emptyConnection = (EmptyConnection) connection;

    Session session = emptyConnection.createSession(
        properties
    );
    assertTrue(session instanceof EmptySession);
    session.update(properties);

    StreamReader streamReader = emptyConnection.createStreamReader(
        session,
        null,
        properties,
        null
    );
    assertTrue(streamReader instanceof EmptyStreamReader);
    streamReader.update(properties);
    streamReader.read(null, null);
    streamReader.returnLoan(null, null);

    StreamWriter streamWriter = emptyConnection.createStreamWriter(
        session,
        null,
        properties
    );
    assertTrue(streamWriter instanceof EmptyStreamWriter);
    streamWriter.update(properties);
    streamWriter.write(null, null);

    StreamReader inputDiscoveryStreamReader = emptyConnection.getInputStreamDiscoveryReader();
    assertTrue(inputDiscoveryStreamReader instanceof EmptyStreamReader);

    StreamReader outputDiscoveryStreamReader = emptyConnection.getOutputStreamDiscoveryReader();
    assertTrue(outputDiscoveryStreamReader instanceof EmptyStreamReader);

    emptyConnection.update(properties);

    try {
      emptyConnection.copyTypeRepresentation(new Object());
      fail("AdapterException was expected.");
    } catch (AdapterException ex) {
      // exception was expected
    }

    emptyConnection.deleteTypeRepresentation(new Object());
    emptyConnection.deleteStreamWriter(streamWriter);
    emptyConnection.deleteStreamReader(streamReader);
    emptyConnection.deleteSession(session);

    adapter.deleteConnection(connection);
  }
}
