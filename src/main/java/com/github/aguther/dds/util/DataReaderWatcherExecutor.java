package com.github.aguther.dds.util;

import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.ReadCondition;

public interface DataReaderWatcherExecutor<T> {

  void execute(
      DataReader dataReader,
      ReadCondition readCondition,
      DataReaderWatcherListener<T> listener
  );

}
