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

import com.rti.routingservice.adapter.StreamReader;
import com.rti.routingservice.adapter.infrastructure.AdapterException;
import java.util.List;
import java.util.Properties;

/**
 * This class implements a stream reader that does nothing.
 */
public class EmptyStreamReader implements StreamReader {

  @Override
  public void read(
      final List<Object> sampleList,
      final List<Object> infoList
  ) throws AdapterException {
    // do nothing
  }

  @Override
  public void returnLoan(
      final List<Object> sampleList,
      final List<Object> infoList
  ) throws AdapterException {
    // do nothing

  }

  @Override
  public void update(
      final Properties properties
  ) throws AdapterException {
    // do nothing
  }
}
