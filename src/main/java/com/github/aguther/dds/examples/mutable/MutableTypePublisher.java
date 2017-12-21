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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.Cookie_t;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.Locator_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.AcknowledgmentInfo;
import com.rti.dds.publication.DataWriter;
import com.rti.dds.publication.DataWriterListener;
import com.rti.dds.publication.LivelinessLostStatus;
import com.rti.dds.publication.OfferedDeadlineMissedStatus;
import com.rti.dds.publication.OfferedIncompatibleQosStatus;
import com.rti.dds.publication.PublicationMatchedStatus;
import com.rti.dds.publication.ReliableReaderActivityChangedStatus;
import com.rti.dds.publication.ReliableWriterCacheChangedStatus;
import com.rti.dds.publication.ServiceRequestAcceptedStatus;
import idl.v1.MutableType;
import idl.v1.UnionTypeDiscriminant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MutableTypePublisher implements Runnable, DataWriterListener {

  private static final Logger log;

  static {
    log = LoggerFactory.getLogger(MutableTypePublisher.class);
  }

  private boolean shouldTerminate;
  private DataWriter dataWriter;
  private int sleepTime;

  private int counter;

  MutableTypePublisher(
      DomainParticipant domainParticipant,
      String dataWriterName,
      int sleepTime
  ) {
    this(
        domainParticipant.lookup_datawriter_by_name(dataWriterName),
        sleepTime
    );
  }

  MutableTypePublisher(
      DataWriter dataWriter,
      int sleepTime
  ) {
    checkNotNull(dataWriter);
    checkArgument(sleepTime >= 0);

    this.dataWriter = dataWriter;
    this.sleepTime = sleepTime;
  }

  void stop() {
    shouldTerminate = true;
  }

  @Override
  public void run() {
    log.info("Start sending ...");

    dataWriter.set_listener(this, StatusKind.STATUS_MASK_ALL);

    while (!shouldTerminate) {
      try {
        // create sample
        MutableType sample = generateSample();

        // log number of sample
        log.info(
            "Writing sample: key='{}', union='{}', array[0].number='{}'",
            sample.key,
            sample.unionType._d,
            sample.arrayType[0].number
        );

        // write sample
        dataWriter.write_untyped(sample, InstanceHandle_t.HANDLE_NIL);

        // wait some time
        if (sleepTime >= 0) {
          Thread.sleep(sleepTime);
        }

      } catch (RETCODE_ERROR e) {
        // log the problem and sTerminate the application
        log.error("Failed to write sample.", e);
      } catch (InterruptedException e) {
        log.error("Failed to wait.", e);
        Thread.currentThread().interrupt();
      }
    }

    dataWriter.set_listener(null, StatusKind.STATUS_MASK_NONE);

    log.info("... done.");
  }

  private MutableType generateSample() {
    // create sample
    MutableType sample = new MutableType();

    sample.key = 0;
    sample.unionType._d = UnionTypeDiscriminant.TWO;
    sample.unionType.two.number = 2;
    sample.unionType.two.text = "TWO";
    for (int i = 0; i < sample.arrayType.length; i++) {
      sample.arrayType[i].number = counter;
    }
    counter++;

    // print sample
    if (log.isDebugEnabled()) {
      log.debug("Created sample: '{}'", sample.toString().replace("\n", " "));
    }

    // return the result
    return sample;
  }

  @Override
  public void on_offered_deadline_missed(
      DataWriter dataWriter,
      OfferedDeadlineMissedStatus offeredDeadlineMissedStatus
  ) {
    if (log.isWarnEnabled()) {
      log.warn("{}", offeredDeadlineMissedStatus.toString());
    }
  }

  @Override
  public void on_offered_incompatible_qos(
      DataWriter dataWriter,
      OfferedIncompatibleQosStatus offeredIncompatibleQosStatus
  ) {
    if (log.isWarnEnabled()) {
      log.warn("{}", offeredIncompatibleQosStatus.toString());
    }
  }

  @Override
  public void on_liveliness_lost(
      DataWriter dataWriter,
      LivelinessLostStatus livelinessLostStatus
  ) {
    if (log.isDebugEnabled()) {
      log.debug("{}", livelinessLostStatus.toString());
    }
  }

  @Override
  public void on_publication_matched(
      DataWriter dataWriter,
      PublicationMatchedStatus publicationMatchedStatus
  ) {
    if (log.isDebugEnabled()) {
      log.debug("{}", publicationMatchedStatus.toString());
    }
  }

  @Override
  public void on_reliable_writer_cache_changed(
      DataWriter dataWriter,
      ReliableWriterCacheChangedStatus reliableWriterCacheChangedStatus
  ) {
    if (log.isDebugEnabled()) {
      log.debug("{}", reliableWriterCacheChangedStatus.toString());
    }
  }

  @Override
  public void on_reliable_reader_activity_changed(
      DataWriter dataWriter,
      ReliableReaderActivityChangedStatus reliableReaderActivityChangedStatus
  ) {
    if (log.isDebugEnabled()) {
      log.debug("{}", reliableReaderActivityChangedStatus.toString());
    }
  }

  @Override
  public void on_destination_unreachable(
      DataWriter dataWriter,
      InstanceHandle_t instanceHandle,
      Locator_t locator
  ) {
    if (log.isInfoEnabled()) {
      log.info("{}; {}", instanceHandle.toString(), locator.toString());
    }
  }

  @Override
  public Object on_data_request(
      DataWriter dataWriter,
      Cookie_t cookie
  ) {
    return null;
  }

  @Override
  public void on_data_return(
      DataWriter dataWriter,
      Object o,
      Cookie_t cookie
  ) {
    if (log.isDebugEnabled()) {
      log.debug("{} {}", o.toString(), cookie.toString());
    }
  }

  @Override
  public void on_sample_removed(
      DataWriter dataWriter,
      Cookie_t cookie
  ) {
    if (log.isWarnEnabled()) {
      log.warn("{}", cookie.toString());
    }
  }

  @Override
  public void on_instance_replaced(
      DataWriter dataWriter,
      InstanceHandle_t instanceHandle
  ) {
    if (log.isWarnEnabled()) {
      log.warn("{}", instanceHandle.toString());
    }
  }

  @Override
  public void on_application_acknowledgment(
      DataWriter dataWriter,
      AcknowledgmentInfo acknowledgmentInfo
  ) {
    if (log.isInfoEnabled()) {
      log.info("{}", acknowledgmentInfo.toString());
    }
  }

  @Override
  public void on_service_request_accepted(
      DataWriter dataWriter,
      ServiceRequestAcceptedStatus serviceRequestAcceptedStatus
  ) {
    if (log.isInfoEnabled()) {
      log.info("{}", serviceRequestAcceptedStatus.toString());
    }
  }
}
