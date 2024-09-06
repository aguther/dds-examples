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

package io.github.aguther.dds.support.subscription;

import static com.google.common.base.Preconditions.checkNotNull;

import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.ReadCondition;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.util.LoanableSequence;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SampleReader<T> implements DataReaderWatcherExecutor<T> {

  private static final Logger LOGGER = LogManager.getLogger(SampleTaker.class);

  private List<T> sampleSeq;
  private SampleInfoSeq sampleInfoSeq;

  @SuppressWarnings("unchecked")
  public SampleReader(
    LoanableSequence sampleSeq
  ) {
    checkNotNull(sampleSeq);

    this.sampleSeq = sampleSeq;
    this.sampleInfoSeq = new SampleInfoSeq();
  }

  public void execute(
    DataReader dataReader,
    ReadCondition readCondition,
    OnDataAvailableListener<T> listener
  ) {
    try {
      // take data
      dataReader.read_w_condition_untyped(
        sampleSeq,
        sampleInfoSeq,
        ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
        readCondition
      );

      // iterate over data
      for (int i = 0; i < sampleSeq.size(); i++) {
        listener.onDataAvailable(
          dataReader,
          sampleSeq.get(i),
          sampleInfoSeq.get(i)
        );
      }

    } catch (Exception ex) {
      LOGGER.error("Exception during listener invocation", ex);
    } finally {
      // return data
      dataReader.return_loan_untyped(
        sampleSeq,
        sampleInfoSeq
      );
    }
  }
}
