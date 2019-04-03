package com.github.aguther.dds.routing.dynamic.command.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.github.aguther.dds.routing.dynamic.observer.Session;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute.Direction;
import idl.RTI.Service.Admin.CommandRequest;
import org.junit.Before;
import org.junit.Test;

public class CommandTest {

  private static final String TOPIC = "Square";
  private static final String TYPE = "ShapeType";

  private Session sessionEmpty;
  private Session sessionSquareA;
  private Session sessionSquareB;
  private TopicRoute topicRouteOutSquareShapeType;
  private TopicRoute topicRouteInSquareShapeType;

  @Before
  public void setUp() {
    sessionEmpty = new Session("", "");
    sessionSquareA = new Session(TOPIC, "A");
    sessionSquareB = new Session(TOPIC, "B");
    topicRouteOutSquareShapeType = new TopicRoute(Direction.OUT, TOPIC, TYPE);
    topicRouteInSquareShapeType = new TopicRoute(Direction.IN, TOPIC, TYPE);
  }

  @Test
  public void testGetter() {
    CommandType commandType = CommandType.COMMAND_TYPE_DELETE;
    Session session = sessionSquareA;
    TopicRoute topicRoute = topicRouteOutSquareShapeType;
    CommandRequest commandRequest = new CommandRequest();
    String loggingFormat = "A";

    Command command = new Command(
        commandType,
        session,
        topicRoute,
        commandRequest,
        loggingFormat
    );

    assertEquals(commandType, command.getType());
    assertEquals(session, command.getSession());
    assertEquals(topicRoute, command.getTopicRoute());
    assertEquals(commandRequest, command.getCommandRequest());
    assertEquals(loggingFormat, command.getLoggingFormat());
  }

  @Test
  public void testEqualsSameObject() {
    Command commandA = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    assertEquals(commandA, commandA);
  }

  @Test
  public void testEqualsSameContent() {
    Command commandA = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    Command commandB = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    assertEquals(commandA, commandB);
  }

  @Test
  public void testEqualsOtherObjectType() {
    Command commandA = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    assertNotEquals(commandA, sessionEmpty);
  }

  @Test
  public void testEqualsCommandType() {
    Command commandA = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    Command commandB = new Command(
        CommandType.COMMAND_TYPE_DELETE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    assertNotEquals(commandA, commandB);
  }

  @Test
  public void testEqualsSession() {
    Command commandA = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    Command commandB = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareB,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    assertNotEquals(commandA, commandB);
  }

  @Test
  public void testEqualsTopicRoute() {
    Command commandA = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    Command commandB = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteInSquareShapeType,
        new CommandRequest(),
        "A"
    );
    assertNotEquals(commandA, commandB);
  }

  @Test
  public void testEqualsCommandRequest() {
    Command commandA = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    Command commandB = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        null,
        "A"
    );
    assertEquals(commandA, commandB);
  }

  @Test
  public void testEqualsLoggingFormat() {
    Command commandA = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    Command commandB = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "B"
    );
    assertEquals(commandA, commandB);
  }

  @Test
  public void testHashCodeSameObject() {
    Command commandA = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    assertEquals(commandA.hashCode(), commandA.hashCode());
  }

  @Test
  public void testHashCodeSameContent() {
    Command commandA = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    Command commandB = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    assertEquals(commandA.hashCode(), commandB.hashCode());
  }

  @Test
  public void testHashCodeCommandType() {
    Command commandA = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    Command commandB = new Command(
        CommandType.COMMAND_TYPE_DELETE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    assertNotEquals(commandA.hashCode(), commandB.hashCode());
  }

  @Test
  public void testHashCodeSession() {
    Command commandA = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    Command commandB = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareB,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    assertNotEquals(commandA.hashCode(), commandB.hashCode());
  }

  @Test
  public void testHashCodeTopicRoute() {
    Command commandA = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    Command commandB = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteInSquareShapeType,
        new CommandRequest(),
        "A"
    );
    assertNotEquals(commandA.hashCode(), commandB.hashCode());
  }

  @Test
  public void testHashCodeCommandRequest() {
    Command commandA = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    Command commandB = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        null,
        "A"
    );
    assertEquals(commandA.hashCode(), commandB.hashCode());
  }

  @Test
  public void testHashCodeLoggingFormat() {
    Command commandA = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "A"
    );
    Command commandB = new Command(
        CommandType.COMMAND_TYPE_CREATE,
        sessionSquareA,
        topicRouteOutSquareShapeType,
        new CommandRequest(),
        "B"
    );
    assertEquals(commandA.hashCode(), commandB.hashCode());
  }

  @Test
  public void testToString() {
    CommandType commandType = CommandType.COMMAND_TYPE_DELETE;
    Session session = sessionSquareA;
    TopicRoute topicRoute = topicRouteOutSquareShapeType;
    CommandRequest commandRequest = new CommandRequest();
    String loggingFormat = "LoggingFormat";

    Command command = new Command(
        commandType,
        session,
        topicRoute,
        commandRequest,
        loggingFormat
    );

    String result = command.toString();

    assertTrue(result.contains(commandType.toString()));
    assertTrue(result.contains(session.toString()));
    assertTrue(result.contains(topicRoute.toString()));
    assertFalse(result.contains(commandRequest.toString()));
    assertFalse(result.contains(loggingFormat));
  }
}