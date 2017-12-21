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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MutableTypeListener implements DataReaderListener {

  private static final Logger log = LoggerFactory.getLogger(MutableTypeListener.class);

  private DataReader dataReader;
  private MutableType sample;
  private SampleInfo sampleInfo;

  MutableTypeListener(
      DataReader dataReader
  ) {
    checkNotNull(dataReader);

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
      DataReader dataReader,
      RequestedDeadlineMissedStatus requestedDeadlineMissedStatus
  ) {
    if (log.isWarnEnabled()) {
      log.warn("{}", requestedDeadlineMissedStatus.toString());
    }
  }

  @Override
  public void on_requested_incompatible_qos(
      DataReader dataReader,
      RequestedIncompatibleQosStatus requestedIncompatibleQosStatus
  ) {
    if (log.isWarnEnabled()) {
      log.warn("{}", requestedIncompatibleQosStatus.toString());
    }
  }

  @Override
  public void on_sample_rejected(
      DataReader dataReader,
      SampleRejectedStatus sampleRejectedStatus
  ) {
    if (log.isWarnEnabled()) {
      log.warn("{}", sampleRejectedStatus.toString());
    }
  }

  @Override
  public void on_liveliness_changed(
      DataReader dataReader,
      LivelinessChangedStatus livelinessChangedStatus
  ) {
    if (log.isDebugEnabled()) {
      log.debug("{}", livelinessChangedStatus.toString());
    }
  }

  @Override
  public void on_data_available(
      DataReader dataReader
  ) {
    do {
      try {
        // get next sample
        dataReader.read_next_sample_untyped(sample, sampleInfo);

        // print info
        if (sampleInfo.valid_data) {
          log.info(
              "Received sample: key='{}', union='{}', array[0].number='{}'",
              sample.key,
              sample.unionType._d,
              sample.arrayType[0].number
          );
        } else {
          log.warn("Invalid sample received.");
        }
      } catch (RETCODE_NO_DATA ex) {
        if (log.isTraceEnabled()) {
          log.trace("{}", ex);
        }
        break;
      }

    } while (sampleInfo.valid_data);
  }

  @Override
  public void on_sample_lost(
      DataReader dataReader,
      SampleLostStatus sampleLostStatus
  ) {
    if (log.isWarnEnabled()) {
      log.warn("{}", sampleLostStatus.toString());
    }
  }

  @Override
  public void on_subscription_matched(
      DataReader dataReader,
      SubscriptionMatchedStatus subscriptionMatchedStatus
  ) {
    if (log.isDebugEnabled()) {
      log.debug("{}", subscriptionMatchedStatus.toString());
    }
  }
}
