package com.github.aguther.dds.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.ReadCondition;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.util.LoanableSequence;
import java.util.List;

public class SampleTaker<T> implements DataReaderWatcherExecutor<T> {

  private List<T> sampleSeq;
  private SampleInfoSeq sampleInfoSeq;

  @SuppressWarnings("unchecked")
  public SampleTaker(
      LoanableSequence sampleSeq
  ) {
    checkNotNull(sampleSeq);

    this.sampleSeq = sampleSeq;
    this.sampleInfoSeq = new SampleInfoSeq();
  }

  public void execute(
      DataReader dataReader,
      ReadCondition readCondition,
      DataReaderWatcherListener<T> listener
  ) {
    try {
      // take data
      dataReader.take_w_condition_untyped(
          sampleSeq,
          sampleInfoSeq,
          ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
          readCondition
      );

      // iterate over data
      for (int i = 0; i < sampleSeq.size(); i++) {
        listener.onDataAvailable(
            sampleSeq.get(i),
            sampleInfoSeq.get(i)
        );
      }

    } finally {
      // return data
      dataReader.return_loan_untyped(
          sampleSeq,
          sampleInfoSeq
      );
    }
  }
}
