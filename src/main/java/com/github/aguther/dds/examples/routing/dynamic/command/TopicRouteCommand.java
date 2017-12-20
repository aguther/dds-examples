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

package com.github.aguther.dds.examples.routing.dynamic.command;

import com.github.aguther.dds.examples.routing.dynamic.observer.Session;
import com.github.aguther.dds.examples.routing.dynamic.observer.TopicRoute;
import java.util.Objects;

public class TopicRouteCommand {

  private Session session;
  private TopicRoute topicRoute;
  private CommandType commandType;

  public TopicRouteCommand(
      Session session,
      TopicRoute topicRoute,
      CommandType commandType
  ) {
    this.session = session;
    this.topicRoute = topicRoute;
    this.commandType = commandType;
  }

  public Session getSession() {
    return session;
  }

  public TopicRoute getTopicRoute() {
    return topicRoute;
  }

  public CommandType getCommandType() {
    return commandType;
  }

  @Override
  public boolean equals(
      Object o
  ) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TopicRouteCommand that = (TopicRouteCommand) o;
    return Objects.equals(session, that.session) &&
        Objects.equals(topicRoute, that.topicRoute);
  }

  @Override
  public int hashCode() {

    return Objects.hash(session, topicRoute);
  }
}
