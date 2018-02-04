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

package com.github.aguther.dds.routing.dynamic.command.remote;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.aguther.dds.routing.dynamic.command.DynamicPartitionCommandProvider;
import com.github.aguther.dds.routing.dynamic.observer.Session;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute.Direction;
import com.github.aguther.dds.routing.util.RoutingServiceCommandInterface;
import idl.RTI.RoutingService.Administration.CommandKind;
import idl.RTI.RoutingService.Administration.CommandRequest;
import idl.RTI.RoutingService.Administration.CommandResponse;
import idl.RTI.RoutingService.Administration.CommandResponseKind;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class DynamicPartitionCommanderTest {

  private CommandResponse commandResponseOk;
  private CommandResponse commandResponseError;

  private RoutingServiceCommandInterface commandInterface;
  private DynamicPartitionCommandProvider commanderProvider;
  private CommandBuilder commandBuilder;

  private DynamicPartitionCommander commander;

  @Before
  public void setUp() {
    commandResponseOk = new CommandResponse();
    commandResponseOk.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK;

    commandResponseError = new CommandResponse();
    commandResponseError.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_ERROR;

    commandInterface = mock(RoutingServiceCommandInterface.class);
    commanderProvider = mock(DynamicPartitionCommandProvider.class);

    commander = new DynamicPartitionCommander(
        commandInterface,
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
  public void testConstructorNoTimes() {
    DynamicPartitionCommander dynamicPartitionCommander = new DynamicPartitionCommander(
        commandInterface,
        commanderProvider,
        "UnitTest"
    );
    assertNotNull(dynamicPartitionCommander);
  }

  @Test
  public void testConstructorRetryDelay() {
    DynamicPartitionCommander dynamicPartitionCommander = new DynamicPartitionCommander(
        commandInterface,
        commanderProvider,
        "UnitTest",
        100,
        TimeUnit.MILLISECONDS
    );
    assertNotNull(dynamicPartitionCommander);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorTargetRouterNull() {
    DynamicPartitionCommander dynamicPartitionCommander = new DynamicPartitionCommander(
        commandInterface,
        commanderProvider,
        null,
        100,
        TimeUnit.MILLISECONDS
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorTargetRouterEmpty() {
    DynamicPartitionCommander dynamicPartitionCommander = new DynamicPartitionCommander(
        commandInterface,
        commanderProvider,
        "",
        100,
        TimeUnit.MILLISECONDS
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorRetryZero() {
    DynamicPartitionCommander dynamicPartitionCommander = new DynamicPartitionCommander(
        commandInterface,
        commanderProvider,
        "UnitTest",
        0,
        TimeUnit.MILLISECONDS
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorRetryBelowZero() {
    DynamicPartitionCommander dynamicPartitionCommander = new DynamicPartitionCommander(
        commandInterface,
        commanderProvider,
        "UnitTest",
        -1,
        TimeUnit.MILLISECONDS
    );
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorRetryTimeUnitNull() {
    DynamicPartitionCommander dynamicPartitionCommander = new DynamicPartitionCommander(
        commandInterface,
        commanderProvider,
        "UnitTest",
        100,
        null
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorTimeoutZero() {
    DynamicPartitionCommander dynamicPartitionCommander = new DynamicPartitionCommander(
        commandInterface,
        commanderProvider,
        "UnitTest",
        100,
        TimeUnit.MILLISECONDS,
        0,
        TimeUnit.MILLISECONDS
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorTimeoutBelowZero() {
    DynamicPartitionCommander dynamicPartitionCommander = new DynamicPartitionCommander(
        commandInterface,
        commanderProvider,
        "UnitTest",
        100,
        TimeUnit.MILLISECONDS,
        -1,
        TimeUnit.MILLISECONDS
    );
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorTimeoutTimeUnitNull() {
    DynamicPartitionCommander dynamicPartitionCommander = new DynamicPartitionCommander(
        commandInterface,
        commanderProvider,
        "UnitTest",
        100,
        TimeUnit.MILLISECONDS,
        100,
        null
    );
  }

  @Test
  public void testCreateSession() {
    Session session = new Session("Square", "A");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK;

    when(commandInterface.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandInterface.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(commandResponse);
    when(commanderProvider.getSessionConfiguration(any(Session.class)))
        .thenReturn("str://\"<session></session>\"");

    commander.createSession(session);

    verify(commanderProvider, timeout(5000).times(1))
        .getSessionParent(session);
    verify(commanderProvider, timeout(5000).times(1))
        .getSessionConfiguration(session);
    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  public void testCreateSessionTimeout() {
    Session session = new Session("Square", "A");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK;

    when(commandInterface.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandInterface.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(null).thenReturn(null).thenReturn(commandResponse);
    when(commanderProvider.getSessionConfiguration(any(Session.class)))
        .thenReturn("str://\"<session></session>\"");

    commander.createSession(session);

    verify(commanderProvider, timeout(5000).times(1))
        .getSessionParent(session);
    verify(commanderProvider, timeout(5000).times(1))
        .getSessionConfiguration(session);
    verify(commandInterface, timeout(5000).times(3))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  public void testCreateSessionFailed() {
    Session session = new Session("Square", "A");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_ERROR;

    when(commandInterface.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandInterface.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(commandResponse);
    when(commanderProvider.getSessionConfiguration(any(Session.class)))
        .thenReturn("str://\"<session></session>\"");

    commander.createSession(session);

    verify(commanderProvider, timeout(5000).times(1))
        .getSessionParent(session);
    verify(commanderProvider, timeout(5000).times(1))
        .getSessionConfiguration(session);
    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  public void testDeleteSession() {
    Session session = new Session("Square", "A");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK;

    when(commandInterface.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandInterface.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(commandResponse);

    commander.deleteSession(session);

    verify(commanderProvider, timeout(5000).times(1))
        .getSessionEntityName(session);
    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  public void testDeleteSessionFailed() {
    Session session = new Session("Square", "A");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_ERROR;

    when(commandInterface.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandInterface.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(commandResponse);

    commander.deleteSession(session);

    verify(commanderProvider, timeout(5000).times(1))
        .getSessionEntityName(session);
    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  @SuppressWarnings("squid:S2925")
  public void testCreateDeleteSessionWithAbort() {
    Session session = new Session("Square", "A");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK;

    when(commandInterface.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandInterface.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenAnswer(invocation -> {
          Thread.sleep(1000);
          return null;
        });
    when(commanderProvider.getSessionConfiguration(any(Session.class)))
        .thenReturn("str://\"<session></session>\"");

    commander.createSession(session);

    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));

    commander.deleteSession(session);

    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));

    commander.createSession(session);

    verify(commanderProvider, timeout(5000).times(2))
        .getSessionParent(session);
    verify(commanderProvider, timeout(5000).times(2))
        .getSessionConfiguration(session);
    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  public void testCreateTopicRoute() {
    Session session = new Session("Square", "A");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK;

    when(commandInterface.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandInterface.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(commandResponse);
    when(commanderProvider.getTopicRouteConfiguration(any(Session.class), any(TopicRoute.class)))
        .thenReturn("str://\"<topic_route></topic_route>\"");

    commander.createTopicRoute(session, topicRoute);

    verify(commanderProvider, timeout(5000).times(1))
        .getSessionEntityName(session);
    verify(commanderProvider, timeout(5000).times(1))
        .getTopicRouteConfiguration(session, topicRoute);
    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  public void testCreateTopicRouteFailed() {
    Session session = new Session("Square", "A");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_ERROR;

    when(commandInterface.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandInterface.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(commandResponse);
    when(commanderProvider.getTopicRouteConfiguration(any(Session.class), any(TopicRoute.class)))
        .thenReturn("str://\"<topic_route></topic_route>\"");

    commander.createTopicRoute(session, topicRoute);

    verify(commanderProvider, timeout(5000).times(1))
        .getSessionEntityName(session);
    verify(commanderProvider, timeout(5000).times(1))
        .getTopicRouteConfiguration(session, topicRoute);
    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  public void testDeleteTopicRoute() {
    Session session = new Session("Square", "A");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK;

    when(commandInterface.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandInterface.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(commandResponse);

    commander.deleteTopicRoute(session, topicRoute);

    verify(commanderProvider, timeout(5000).times(1))
        .getTopicRouteEntityName(session, topicRoute);
    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  public void testDeleteTopicRouteFailed() {
    Session session = new Session("Square", "A");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_ERROR;

    when(commandInterface.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandInterface.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenReturn(commandResponse);

    commander.deleteTopicRoute(session, topicRoute);

    verify(commanderProvider, timeout(5000).times(1))
        .getTopicRouteEntityName(session, topicRoute);
    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  @SuppressWarnings("squid:S2925")
  public void testCreateDeleteTopicRouteWithAbort() {
    Session session = new Session("Square", "A");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, session.getTopic(), "ShapeType");

    CommandResponse commandResponse = new CommandResponse();
    commandResponse.kind = CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK;

    when(commandInterface.createCommandRequest()).thenReturn(new CommandRequest());
    when(commandInterface.sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class)))
        .thenAnswer(invocation -> {
          Thread.sleep(100);
          return null;
        });
    when(commanderProvider.getTopicRouteConfiguration(any(Session.class), any(TopicRoute.class)))
        .thenReturn("str://\"<topic_route></topic_route>\"");

    commander.createTopicRoute(session, topicRoute);

    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));

    commander.deleteTopicRoute(session, topicRoute);

    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));

    commander.createTopicRoute(session, topicRoute);

    verify(commanderProvider, timeout(5000).times(2))
        .getSessionEntityName(session);
    verify(commanderProvider, timeout(5000).times(2))
        .getTopicRouteConfiguration(session, topicRoute);
    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(any(CommandRequest.class), anyLong(), any(TimeUnit.class));
  }

  @Test
  @SuppressWarnings("squid:S2925")
  // A -> run -> success -> end
  public void testCaseFlowRunSuccess() {

    // replace command builder with mock
    CommandBuilder commandBuilder = mock(CommandBuilder.class);
    Whitebox.setInternalState(commander, "commandBuilder", commandBuilder);

    Session session = new Session("Square", "A");

    CommandRequest commandRequestCreate = new CommandRequest();
    commandRequestCreate.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    Command commandCreate = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        session,
        null,
        commandRequestCreate,
        "Logging"
    );

    when(commandBuilder.buildCreateSessionCommand(eq(session))).thenReturn(commandCreate);

    when(commandInterface.sendRequest(
        eq(commandRequestCreate),
        anyLong(),
        any(TimeUnit.class))
    ).thenAnswer(invocation -> {
      return commandResponseOk;
    });

    commander.createSession(session);

    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(eq(commandRequestCreate), anyLong(), any(TimeUnit.class));
  }

  @Test
  @SuppressWarnings("squid:S2925")
  // A -> run -> failed -> wait retry -> run -> success -> end
  public void testCaseFlowRunFailedWaitRetryRunSuccess() {
    // replace command builder with mock
    CommandBuilder commandBuilder = mock(CommandBuilder.class);
    Whitebox.setInternalState(commander, "commandBuilder", commandBuilder);

    Session session = new Session("Square", "A");

    CommandRequest commandRequestCreate = new CommandRequest();
    commandRequestCreate.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    Command commandCreate = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        session,
        null,
        commandRequestCreate,
        "Logging"
    );

    when(commandBuilder.buildCreateSessionCommand(eq(session))).thenReturn(commandCreate);

    when(commandInterface.sendRequest(
        eq(commandRequestCreate),
        anyLong(),
        any(TimeUnit.class))
    ).thenAnswer(invocation -> {
      return commandResponseError;
    }).thenAnswer(invocation -> {
      return commandResponseOk;
    });

    commander.createSession(session);

    verify(commandInterface, timeout(5000).times(2))
        .sendRequest(eq(commandRequestCreate), anyLong(), any(TimeUnit.class));
  }

  @Test
  @SuppressWarnings("squid:S2925")
  // A -> run -> failed -> wait retry -> run -> success -> end
  public void testCaseFlowRunTimeoutWaitRetryRunSuccess() {
    // replace command builder with mock
    CommandBuilder commandBuilder = mock(CommandBuilder.class);
    Whitebox.setInternalState(commander, "commandBuilder", commandBuilder);

    Session session = new Session("Square", "A");

    CommandRequest commandRequestCreate = new CommandRequest();
    commandRequestCreate.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    Command commandCreate = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        session,
        null,
        commandRequestCreate,
        "Logging"
    );

    when(commandBuilder.buildCreateSessionCommand(eq(session))).thenReturn(commandCreate);

    when(commandInterface.sendRequest(
        eq(commandRequestCreate),
        anyLong(),
        any(TimeUnit.class))
    ).thenAnswer(invocation -> {
      return null;
    }).thenAnswer(invocation -> {
      return commandResponseOk;
    });

    commander.createSession(session);

    verify(commandInterface, timeout(5000).times(2))
        .sendRequest(eq(commandRequestCreate), anyLong(), any(TimeUnit.class));
  }

  @Test
  @SuppressWarnings("squid:S2925")
  // Case 3: A -> run -> failed -> wait retry -> B -> abort (A) -> run (B) -> success -> end
  public void testCaseFlowRunFailedWaitRetryAbortRunSuccess() throws InterruptedException {
    // replace command builder with mock
    CommandBuilder commandBuilder = mock(CommandBuilder.class);
    Whitebox.setInternalState(commander, "commandBuilder", commandBuilder);

    Session session = new Session("Square", "A");

    CommandRequest commandRequestCreate = new CommandRequest();
    commandRequestCreate.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    Command commandCreate = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        session,
        null,
        commandRequestCreate,
        "Logging"
    );

    CommandRequest commandRequestDelete = new CommandRequest();
    commandRequestDelete.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    Command commandDelete = new Command(
        CommandType.COMMAND_TYPE_DELETE,
        session,
        null,
        commandRequestDelete,
        "Logging"
    );

    when(commandBuilder.buildCreateSessionCommand(eq(session))).thenReturn(commandCreate);
    when(commandBuilder.buildDeleteSessionCommand(eq(session))).thenReturn(commandDelete);

    when(commandInterface.sendRequest(
        eq(commandRequestCreate),
        anyLong(),
        any(TimeUnit.class))
    ).thenAnswer(invocation -> {
      Thread.sleep(0);
      return commandResponseError;
    });
    when(commandInterface.sendRequest(
        eq(commandRequestDelete),
        anyLong(),
        any(TimeUnit.class))
    ).thenAnswer(invocation -> {
      Thread.sleep(0);
      return commandResponseOk;
    });

    commander.createSession(session);
    Thread.sleep(25);
    commander.deleteSession(session);

    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(eq(commandRequestCreate), anyLong(), any(TimeUnit.class));
    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(eq(commandRequestDelete), anyLong(), any(TimeUnit.class));
  }

  @Test
  @SuppressWarnings("squid:S2925")
  // A -> run -> B -> abort (A) -> success (A) -> run (B) -> success (B) -> end
  public void testCaseFlowRunAbortSuccessRunSuccess() throws InterruptedException {
    // replace command builder with mock
    CommandBuilder commandBuilder = mock(CommandBuilder.class);
    Whitebox.setInternalState(commander, "commandBuilder", commandBuilder);

    Session session = new Session("Square", "A");

    CommandRequest commandRequestCreate = new CommandRequest();
    commandRequestCreate.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    Command commandCreate = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        session,
        null,
        commandRequestCreate,
        "Logging"
    );

    CommandRequest commandRequestDelete = new CommandRequest();
    commandRequestDelete.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    Command commandDelete = new Command(
        CommandType.COMMAND_TYPE_DELETE,
        session,
        null,
        commandRequestDelete,
        "Logging"
    );

    when(commandBuilder.buildCreateSessionCommand(eq(session))).thenReturn(commandCreate);
    when(commandBuilder.buildDeleteSessionCommand(eq(session))).thenReturn(commandDelete);

    when(commandInterface.sendRequest(
        eq(commandRequestCreate),
        anyLong(),
        any(TimeUnit.class))
    ).thenAnswer(invocation -> {
      Thread.sleep(75);
      return commandResponseOk;
    });
    when(commandInterface.sendRequest(
        eq(commandRequestDelete),
        anyLong(),
        any(TimeUnit.class))
    ).thenAnswer(invocation -> {
      Thread.sleep(0);
      return commandResponseOk;
    });

    commander.createSession(session);
    Thread.sleep(25);
    commander.deleteSession(session);

    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(eq(commandRequestCreate), anyLong(), any(TimeUnit.class));
    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(eq(commandRequestDelete), anyLong(), any(TimeUnit.class));
  }

  @Test
  @SuppressWarnings("squid:S2925")
  // A -> run -> B -> abort (A) -> failed (A) -> end
  public void testCaseFlowRunAbortFailed() throws InterruptedException {
    // replace command builder with mock
    CommandBuilder commandBuilder = mock(CommandBuilder.class);
    Whitebox.setInternalState(commander, "commandBuilder", commandBuilder);

    Session session = new Session("Square", "A");

    CommandRequest commandRequestCreate = new CommandRequest();
    commandRequestCreate.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    Command commandCreate = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        session,
        null,
        commandRequestCreate,
        "Logging"
    );

    CommandRequest commandRequestDelete = new CommandRequest();
    commandRequestDelete.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    Command commandDelete = new Command(
        CommandType.COMMAND_TYPE_DELETE,
        session,
        null,
        commandRequestDelete,
        "Logging"
    );

    when(commandBuilder.buildCreateSessionCommand(eq(session))).thenReturn(commandCreate);
    when(commandBuilder.buildDeleteSessionCommand(eq(session))).thenReturn(commandDelete);

    when(commandInterface.sendRequest(
        eq(commandRequestCreate),
        anyLong(),
        any(TimeUnit.class))
    ).thenAnswer(invocation -> {
      Thread.sleep(75);
      return commandResponseError;
    });
    when(commandInterface.sendRequest(
        eq(commandRequestDelete),
        anyLong(),
        any(TimeUnit.class))
    ).thenAnswer(invocation -> {
      Thread.sleep(0);
      return commandResponseOk;
    });

    commander.createSession(session);
    Thread.sleep(25);
    commander.deleteSession(session);

    verify(commandInterface, timeout(5000).times(1))
        .sendRequest(eq(commandRequestCreate), anyLong(), any(TimeUnit.class));
    verify(commandInterface, timeout(5000).times(0))
        .sendRequest(eq(commandRequestDelete), anyLong(), any(TimeUnit.class));
  }
}
