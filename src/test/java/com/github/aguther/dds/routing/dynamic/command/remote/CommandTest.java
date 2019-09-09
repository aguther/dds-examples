package com.github.aguther.dds.routing.dynamic.command.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.github.aguther.dds.routing.dynamic.observer.Direction;
import com.github.aguther.dds.routing.dynamic.observer.Session;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import idl.RTI.RoutingService.Administration.CommandRequest;
import org.junit.Test;

public class CommandTest {

  @Test
  public void testGetter() {
    CommandType commandType = CommandType.COMMAND_TYPE_DELETE;
    Session session = new Session(Direction.OUT, "Square", "A");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, "Square", "ShapeType");
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
    Command A = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    assertEquals(A, A);
  }

  @Test
  public void testEqualsSameContent() {
    Command A = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    Command B = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    assertEquals(A, B);
  }

  @Test
  public void testEqualsOtherObjectType() {
    Command A = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    assertNotEquals(A, new Session(Direction.OUT, "", ""));
  }

  @Test
  public void testEqualsCommandType() {
    Command A = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    Command B = new Command(
      CommandType.COMMAND_TYPE_DELETE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    assertNotEquals(A, B);
  }

  @Test
  public void testEqualsSession() {
    Command A = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    Command B = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "B"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    assertNotEquals(A, B);
  }

  @Test
  public void testEqualsTopicRoute() {
    Command A = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    Command B = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.IN, "Square", "A"),
      new TopicRoute(Direction.IN, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    assertNotEquals(A, B);
  }

  @Test
  public void testEqualsCommandRequest() {
    Command A = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    Command B = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      null,
      "A"
    );
    assertEquals(A, B);
  }

  @Test
  public void testEqualsLoggingFormat() {
    Command A = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    Command B = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "B"
    );
    assertEquals(A, B);
  }

  @Test
  public void testHashCodeSameObject() {
    Command A = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    assertEquals(A.hashCode(), A.hashCode());
  }

  @Test
  public void testHashCodeSameContent() {
    Command A = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    Command B = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    assertEquals(A.hashCode(), B.hashCode());
  }

  @Test
  public void testHashCodeCommandType() {
    Command A = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    Command B = new Command(
      CommandType.COMMAND_TYPE_DELETE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    assertNotEquals(A.hashCode(), B.hashCode());
  }

  @Test
  public void testHashCodeSession() {
    Command A = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    Command B = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "B"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    assertNotEquals(A.hashCode(), B.hashCode());
  }

  @Test
  public void testHashCodeTopicRoute() {
    Command A = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    Command B = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.IN, "Square", "A"),
      new TopicRoute(Direction.IN, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    assertNotEquals(A.hashCode(), B.hashCode());
  }

  @Test
  public void testHashCodeCommandRequest() {
    Command A = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    Command B = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      null,
      "A"
    );
    assertEquals(A.hashCode(), B.hashCode());
  }

  @Test
  public void testHashCodeLoggingFormat() {
    Command A = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "A"
    );
    Command B = new Command(
      CommandType.COMMAND_TYPE_CREATE,
      new Session(Direction.OUT, "Square", "A"),
      new TopicRoute(Direction.OUT, "Square", "ShapeType"),
      new CommandRequest(),
      "B"
    );
    assertEquals(A.hashCode(), B.hashCode());
  }

  @Test
  public void testToString() {
    CommandType commandType = CommandType.COMMAND_TYPE_DELETE;
    Session session = new Session(Direction.OUT, "Square", "A");
    TopicRoute topicRoute = new TopicRoute(Direction.OUT, "Square", "ShapeType");
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
