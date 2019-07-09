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

package com.github.aguther.dds.support;

import com.google.common.base.Preconditions;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.ViewStateKind;

public class CrudSelectorNotAliveNoWriters implements CrudSelector {

  @Override
  public CrudFunction select(SampleInfo info) {
    // ensure sample info is not null
    Preconditions.checkNotNull(info);

    // check if data is valid and instance is alive
    if (info.valid_data
      && info.instance_state == InstanceStateKind.ALIVE_INSTANCE_STATE
      && info.sample_state == SampleStateKind.NOT_READ_SAMPLE_STATE) {
      // check if instance is new
      if (info.view_state == ViewStateKind.NEW_VIEW_STATE) {
        return CrudFunction.ADD;
      } else {
        return CrudFunction.MODIFY;
      }
    } else {
      // when instance is not alive
      switch (info.instance_state) {
        case InstanceStateKind.NOT_ALIVE_INSTANCE_STATE:
        case InstanceStateKind.NOT_ALIVE_DISPOSED_INSTANCE_STATE:
        case InstanceStateKind.NOT_ALIVE_NO_WRITERS_INSTANCE_STATE:
          return CrudFunction.DELETE;

        default:
          return CrudFunction.NONE;
      }
    }
  }
}
