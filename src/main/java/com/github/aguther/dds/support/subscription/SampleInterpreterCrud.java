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

package com.github.aguther.dds.support.subscription;

import static com.google.common.base.Preconditions.checkNotNull;

import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.SampleInfo;

public class SampleInterpreterCrud<T> implements OnDataAvailableListener<T> {

  private final CrudSelector crudSelector;
  private CrudListener<T> listener;

  public SampleInterpreterCrud(
    CrudSelector crudSelector,
    CrudListener<T> listener
  ) {
    checkNotNull(crudSelector);
    this.crudSelector = crudSelector;
    checkNotNull(listener);
    this.listener = listener;
  }

  @Override
  public void onDataAvailable(
    DataReader dataReader,
    T sample,
    SampleInfo info
  ) {
    // interpret how to treat samples
    switch (crudSelector.select(info)) {
      case ADD:
        listener.add(sample);
        break;

      case MODIFY:
        listener.modify(sample);
        break;

      case DELETE:
        dataReader.get_key_value_untyped(sample, info.instance_handle);
        listener.delete(sample);
        break;

      default:
        // no action
    }
  }
}
