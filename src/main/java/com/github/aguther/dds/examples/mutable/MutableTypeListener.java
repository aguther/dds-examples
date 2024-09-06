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

package com.github.aguther.dds.examples.mutable;

import static com.google.common.base.Preconditions.checkNotNull;

import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderListener;
import com.rti.dds.subscription.LivelinessChangedStatus;
import com.rti.dds.subscription.RequestedDeadlineMissedStatus;
import com.rti.dds.subscription.RequestedIncompatibleQosStatus;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleLostStatus;
import com.rti.dds.subscription.SampleRejectedStatus;
import com.rti.dds.subscription.SubscriptionMatchedStatus;
import idl.v2.MutableType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MutableTypeListener implements DataReaderListener {

  private static final Logger LOGGER = LogManager.getLogger(MutableTypeListener.class);

  private final DataReader dataReader;
  private final MutableType sample;
  private final SampleInfo sampleInfo;

  MutableTypeListener(
    DataReader dataReader
  ) {
    checkNotNull(dataReader, "DataReader must not be null");

    // set this as listener
    this.dataReader = dataReader;
    dataReader.set_listener(
      this,
      StatusKind.STATUS_MASK_ALL
    );

    // store sample
    this.sample = new MutableType();
    this.sampleInfo = new SampleInfo();
  }

  void stop() {
    dataReader.set_listener(
      null,
      StatusKind.STATUS_MASK_NONE
    );
  }

  @Override
  public void on_requested_deadline_missed(
    final DataReader dataReader,
    final RequestedDeadlineMissedStatus requestedDeadlineMissedStatus
  ) {
    if (LOGGER.isWarnEnabled()) {
      LOGGER.warn("{}", requestedDeadlineMissedStatus.toString());
    }
  }

  @Override
  public void on_requested_incompatible_qos(
    final DataReader dataReader,
    final RequestedIncompatibleQosStatus requestedIncompatibleQosStatus
  ) {
    if (LOGGER.isWarnEnabled()) {
      LOGGER.warn("{}", requestedIncompatibleQosStatus.toString());
    }
  }

  @Override
  public void on_sample_rejected(
    final DataReader dataReader,
    final SampleRejectedStatus sampleRejectedStatus
  ) {
    if (LOGGER.isWarnEnabled()) {
      LOGGER.warn("{}", sampleRejectedStatus.toString());
    }
  }

  @Override
  public void on_liveliness_changed(
    final DataReader dataReader,
    final LivelinessChangedStatus livelinessChangedStatus
  ) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("{}", livelinessChangedStatus.toString());
    }
  }

  @Override
  public void on_data_available(
    final DataReader dataReader
  ) {
    do {
      try {
        // get next sample
        dataReader.read_next_sample_untyped(sample, sampleInfo);

        // print info
        if (sampleInfo.valid_data) {
          LOGGER.info(
            "Received sample: key='{}', union='{}', array[0].number='{}'",
            sample.key,
            sample.unionType._d,
            sample.arrayType[0].number
          );
        } else {
          LOGGER.warn("Invalid sample received.");
        }
      } catch (RETCODE_NO_DATA ex) {
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("{}", ex);
        }
        break;
      }

    } while (sampleInfo.valid_data);
  }

  @Override
  public void on_sample_lost(
    final DataReader dataReader,
    final SampleLostStatus sampleLostStatus
  ) {
    if (LOGGER.isWarnEnabled()) {
      LOGGER.warn("{}", sampleLostStatus.toString());
    }
  }

  @Override
  public void on_subscription_matched(
    final DataReader dataReader,
    final SubscriptionMatchedStatus subscriptionMatchedStatus
  ) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("{}", subscriptionMatchedStatus.toString());
    }
  }
}
