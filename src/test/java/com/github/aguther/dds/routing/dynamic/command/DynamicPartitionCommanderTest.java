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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.aguther.dds.routing.dynamic.observer.Session;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute.Direction;
import com.github.aguther.dds.util.RoutingServiceCommandHelper;
import idl.RTI.RoutingService.Administration.CommandRequest;
import idl.RTI.RoutingService.Administration.CommandResponse;
import idl.RTI.RoutingService.Administration.CommandResponseKind;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DynamicPartitionCommanderTest {

  private RoutingServiceCommandHelper commandHelper;
  private DynamicPartitionCommanderProvider commanderProvider;

  private DynamicPartitionCommander commander;

  @Before
  public void setUp() {
    commandHelper = mock(RoutingServiceCommandHelper.class);
    commanderProvider = mock(DynamicPartitionCommanderProvider.class);

    commander = new DynamicPartitionCommander(
        commandHelper,
        commanderProvider,
        "UnitTest",
        100,
        TimeUnit.MILLISECONDS,
        100,
        TimeUnit.MILLISECONDS
    );
  }

  @After
  public void tearDown() {
    commander.close();
  }

  @Test
  public void testCreateSession() {
    Session session = new Session("Square", "A");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK;

    when(commandHelper.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandHelper.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(commandResponse);

    commander.createSession(session);

    verify(commanderProvider, timeout(5000).times(1))
        .getSessionParent(session);
    verify(commanderProvider, timeout(5000).times(1))
        .getSessionConfiguration(session);
    verify(commandHelper, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  public void testCreateSessionTimeout() {
    Session session = new Session("Square", "A");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK;

    when(commandHelper.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandHelper.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(null).thenReturn(null).thenReturn(commandResponse);

    commander.createSession(session);

    verify(commanderProvider, timeout(5000).times(1))
        .getSessionParent(session);
    verify(commanderProvider, timeout(5000).times(1))
        .getSessionConfiguration(session);
    verify(commandHelper, timeout(5000).times(3))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  public void testCreateSessionFailed() {
    Session session = new Session("Square", "A");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_ERROR;

    when(commandHelper.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandHelper.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(commandResponse);

    commander.createSession(session);

    verify(commanderProvider, timeout(5000).times(1))
        .getSessionParent(session);
    verify(commanderProvider, timeout(5000).times(1))
        .getSessionConfiguration(session);
    verify(commandHelper, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  public void testDeleteSession() {
    Session session = new Session("Square", "A");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK;

    when(commandHelper.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandHelper.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(commandResponse);

    commander.deleteSession(session);

    verify(commanderProvider, timeout(5000).times(1))
        .getSessionEntityName(session);
    verify(commandHelper, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  public void testCreateTopicRoute() {
    Session session = new Session("Square", "A");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK;

    when(commandHelper.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandHelper.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(commandResponse);

    commander.createTopicRoute(session, topicRoute);

    verify(commanderProvider, timeout(5000).times(1))
        .getSessionEntityName(session);
    verify(commanderProvider, timeout(5000).times(1))
        .getTopicRouteConfiguration(session, topicRoute);
    verify(commandHelper, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  public void testDeleteTopicRoute() {
    Session session = new Session("Square", "A");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK;

    when(commandHelper.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandHelper.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(commandResponse);

    commander.deleteTopicRoute(session, topicRoute);

    verify(commanderProvider, timeout(5000).times(1))
        .getTopicRouteEntityName(session, topicRoute);
    verify(commandHelper, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }
}
