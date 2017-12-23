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

package com.github.aguther.dds.routing.dynamic.observer;

import org.junit.Test;

public class DynamicPartitionObserverTest {

  @Test
  public void publicationDiscovered() {
    // ignore publication true
    // ignore publication false
    // ignore partition "" true
    // ignore partition "" false
    // ignore partition A true
    // ignore partition A false

    // session new
    // topic route new
    // session not new
    // topic route new
    // session not new
    // topic route not new
  }

  @Test
  public void publicationLost() {
    // ignore publication true
    // ignore publication false
    // ignore partition "" true
    // ignore partition "" false
    // ignore partition A true
    // ignore partition A false

    // topic route no delete
    // session no delete
    // topic route delete
    // session no delete
    // topic route delete
    // session delete
  }

  @Test
  public void subscriptionDiscovered() {
    // ignore subscription true
    // ignore subscription false
    // ignore partition "" true
    // ignore partition "" false
    // ignore partition A true
    // ignore partition A false

    // session new
    // topic route new
    // session not new
    // topic route new
    // session not new
    // topic route not new
  }

  @Test
  public void subscriptionLost() {
    // ignore subscription true
    // ignore subscription false
    // ignore partition "" true
    // ignore partition "" false
    // ignore partition A true
    // ignore partition A false

    // topic route no delete
    // session no delete
    // topic route delete
    // session no delete
    // topic route delete
    // session delete
  }
}
