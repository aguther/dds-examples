package com.github.aguther.dds.util;

import com.rti.dds.subscription.SampleInfo;

public interface DataReaderWatcherListener<T> {

  void onDataAvailable(
      T sample,
      SampleInfo info
  );

}
