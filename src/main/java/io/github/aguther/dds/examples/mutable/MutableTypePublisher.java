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

package io.github.aguther.dds.examples.mutable;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MutableTypePublisher implements Runnable, DataWriterListener {

  private static final Logger LOGGER = LogManager.getLogger(MutableTypePublisher.class);

  private final DataWriter dataWriter;
  private final int sleepTime;

  private boolean shouldTerminate;
  private int counter;

  MutableTypePublisher(
    final DomainParticipant domainParticipant,
    final String dataWriterName,
    final int sleepTime
  ) {
    this(
      domainParticipant.lookup_datawriter_by_name(dataWriterName),
      sleepTime
    );
  }

  MutableTypePublisher(
    final DataWriter dataWriter,
    final int sleepTime
  ) {
    checkNotNull(dataWriter, "DataWriter must not be null");
    checkArgument(sleepTime >= 0, "Sleep time expected to be 0 or greater");

    this.dataWriter = dataWriter;
    this.sleepTime = sleepTime;
  }

  void stop() {
    shouldTerminate = true;
  }

  @Override
  public void run() {
    LOGGER.info("Start sending ...");

    dataWriter.set_listener(this, StatusKind.STATUS_MASK_ALL);

    while (!shouldTerminate) {
      try {
        // create sample
        MutableType sample = generateSample();

        // log number of sample
        LOGGER.info(
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
        LOGGER.error("Failed to write sample.", e);
      } catch (InterruptedException e) {
        LOGGER.error("Failed to wait.", e);
        Thread.currentThread().interrupt();
      }
    }

    dataWriter.set_listener(null, StatusKind.STATUS_MASK_NONE);

    LOGGER.info("... done.");
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
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Created sample: '{}'", sample.toString().replace("\n", " "));
    }

    // return the result
    return sample;
  }

  @Override
  public void on_offered_deadline_missed(
    final DataWriter dataWriter,
    final OfferedDeadlineMissedStatus offeredDeadlineMissedStatus
  ) {
    if (LOGGER.isWarnEnabled()) {
      LOGGER.warn("{}", offeredDeadlineMissedStatus.toString());
    }
  }

  @Override
  public void on_offered_incompatible_qos(
    final DataWriter dataWriter,
    final OfferedIncompatibleQosStatus offeredIncompatibleQosStatus
  ) {
    if (LOGGER.isWarnEnabled()) {
      LOGGER.warn("{}", offeredIncompatibleQosStatus.toString());
    }
  }

  @Override
  public void on_liveliness_lost(
    final DataWriter dataWriter,
    final LivelinessLostStatus livelinessLostStatus
  ) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("{}", livelinessLostStatus.toString());
    }
  }

  @Override
  public void on_publication_matched(
    final DataWriter dataWriter,
    final PublicationMatchedStatus publicationMatchedStatus
  ) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("{}", publicationMatchedStatus.toString());
    }
  }

  @Override
  public void on_reliable_writer_cache_changed(
    final DataWriter dataWriter,
    final ReliableWriterCacheChangedStatus reliableWriterCacheChangedStatus
  ) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("{}", reliableWriterCacheChangedStatus.toString());
    }
  }

  @Override
  public void on_reliable_reader_activity_changed(
    final DataWriter dataWriter,
    final ReliableReaderActivityChangedStatus reliableReaderActivityChangedStatus
  ) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("{}", reliableReaderActivityChangedStatus.toString());
    }
  }

  @Override
  public void on_destination_unreachable(
    final DataWriter dataWriter,
    final InstanceHandle_t instanceHandle,
    final Locator_t locator
  ) {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("{}; {}", instanceHandle.toString(), locator.toString());
    }
  }

  @Override
  public Object on_data_request(
    final DataWriter dataWriter,
    final Cookie_t cookie
  ) {
    return null;
  }

  @Override
  public void on_data_return(
    final DataWriter dataWriter,
    final Object o,
    final Cookie_t cookie
  ) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("{} {}", o.toString(), cookie.toString());
    }
  }

  @Override
  public void on_sample_removed(
    final DataWriter dataWriter,
    final Cookie_t cookie
  ) {
    if (LOGGER.isWarnEnabled()) {
      LOGGER.warn("{}", cookie.toString());
    }
  }

  @Override
  public void on_instance_replaced(
    final DataWriter dataWriter,
    final InstanceHandle_t instanceHandle
  ) {
    if (LOGGER.isWarnEnabled()) {
      LOGGER.warn("{}", instanceHandle.toString());
    }
  }

  @Override
  public void on_application_acknowledgment(
    final DataWriter dataWriter,
    final AcknowledgmentInfo acknowledgmentInfo
  ) {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("{}", acknowledgmentInfo.toString());
    }
  }

  @Override
  public void on_service_request_accepted(
    final DataWriter dataWriter,
    final ServiceRequestAcceptedStatus serviceRequestAcceptedStatus
  ) {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("{}", serviceRequestAcceptedStatus.toString());
    }
  }
}
