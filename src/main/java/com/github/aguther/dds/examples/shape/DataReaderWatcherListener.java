package com.github.aguther.dds.examples.shape;

import com.rti.dds.subscription.SampleInfo;

public interface DataReaderWatcherListener<T> {

  void onDataAvailable(
      T sample,
      SampleInfo info
  );

}
