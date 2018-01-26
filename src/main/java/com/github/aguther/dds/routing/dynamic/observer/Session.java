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

package com.github.aguther.dds.routing.dynamic.observer;

import java.util.Objects;

public class Session {

  private final String topic;
  private final String partition;

  public Session(
      final String topic
  ) {
    this(topic, "");
  }

  public Session(
      final String topic,
      final String partition
  ) {
    this.topic = topic;
    this.partition = partition;
  }

  public String getTopic() {
    return topic;
  }

  public String getPartition() {
    return partition;
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
    Session session = (Session) o;
    return Objects.equals(topic, session.topic) &&
        Objects.equals(partition, session.partition);
  }

  @Override
  public int hashCode() {

    return Objects.hash(topic, partition);
  }

  @Override
  public String toString() {
    return String.format(
        "Session { topic='%s', partition='%s' }",
        topic,
        partition
    );
  }
}
