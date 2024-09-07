/*
 * MIT License
 *
 * Copyright (c) 2019 Andreas Guther
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

package io.github.aguther.dds.routing.dynamic.observer;

/**
 * Callback interface to get notified when a session or topic route should be created or deleted.
 */
public interface DynamicPartitionObserverListener {

  /**
   * Invoked when a session should be created.
   *
   * @param session session to create
   */
  void createSession(
    final Session session
  );

  /**
   * Invoked when a session should be deleted.
   *
   * @param session session to delete
   */
  void deleteSession(
    final Session session
  );

  /**
   * Invoked when a topic route should be created.
   *
   * @param session    session of topic route
   * @param topicRoute topic route to create
   */
  void createTopicRoute(
    final Session session,
    final TopicRoute topicRoute
  );

  /**
   * Invoked when a topic route should be deleted.
   *
   * @param session    session of topic route
   * @param topicRoute topic route to delete
   */
  void deleteTopicRoute(
    final Session session,
    final TopicRoute topicRoute
  );
}
