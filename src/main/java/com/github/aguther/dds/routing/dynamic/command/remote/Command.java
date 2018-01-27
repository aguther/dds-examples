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

import com.github.aguther.dds.routing.dynamic.observer.Session;
import com.github.aguther.dds.routing.dynamic.observer.TopicRoute;
import idl.RTI.RoutingService.Administration.CommandRequest;
import java.util.Objects;

public class Command {

  private final CommandType type;
  private final Session session;
  private final TopicRoute topicRoute;
  private final CommandRequest commandRequest;
  private final String loggingFormat;

  public Command(
      CommandType type,
      Session session,
      TopicRoute topicRoute,
      CommandRequest commandRequest,
      String loggingFormat
  ) {
    this.type = type;

    this.session = session;
    this.topicRoute = topicRoute;
    this.commandRequest = commandRequest;
    this.loggingFormat = loggingFormat;
  }

  public CommandType getType() {
    return type;
  }

  public Session getSession() {
    return session;
  }

  public TopicRoute getTopicRoute() {
    return topicRoute;
  }

  public CommandRequest getCommandRequest() {
    return commandRequest;
  }

  public String getLoggingFormat() {
    return loggingFormat;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Command)) {
      return false;
    }
    Command command = (Command) o;
    return type == command.type &&
        Objects.equals(session, command.session) &&
        Objects.equals(topicRoute, command.topicRoute);
  }

  @Override
  public int hashCode() {

    return Objects.hash(type, session, topicRoute);
  }

  @Override
  public String toString() {
    return String.format(
        "Command { type='%s', session='%s', topicRoute='%s' }",
        type,
        session,
        topicRoute
    );
  }
}
