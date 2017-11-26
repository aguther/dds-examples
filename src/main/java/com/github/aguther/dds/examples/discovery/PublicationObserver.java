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

package com.github.aguther.dds.examples.discovery;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicDataDataReader;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicDataSeq;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicDataTypeSupport;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.ViewStateKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements an observer for publications.
 */
class PublicationObserver extends BuiltinTopicObserver {

  private static final Logger log;

  static {
    log = LoggerFactory.getLogger(PublicationObserver.class);
  }

  /**
   * Creates a new observer for publications.
   *
   * @param domainParticipant DomainParticipant to use
   * @throws IllegalArgumentException Thrown in case of an error
   */
  PublicationObserver(
      DomainParticipant domainParticipant) {
    // create the parent observer with the built-in publication topic
    super(domainParticipant, PublicationBuiltinTopicDataTypeSupport.PUBLICATION_TOPIC_NAME);
  }

  @Override
  public void on_data_available(DataReader dataReader) {
    // cast the data reader to a typed one
    PublicationBuiltinTopicDataDataReader publicationDataReader = (PublicationBuiltinTopicDataDataReader) dataReader;

    // create data containers
    PublicationBuiltinTopicDataSeq publicationDataSeq = new PublicationBuiltinTopicDataSeq();
    SampleInfoSeq sampleInfoSeq = new SampleInfoSeq();

    // take data from the data reader
    publicationDataReader.take(
        publicationDataSeq,
        sampleInfoSeq,
        ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
        SampleStateKind.ANY_SAMPLE_STATE,
        ViewStateKind.ANY_VIEW_STATE,
        InstanceStateKind.ANY_INSTANCE_STATE);

    // iterate over data received
    for (int i = 0; i < publicationDataSeq.size(); i++) {
      // get sample info
      SampleInfo sampleInfo = sampleInfoSeq.get(i);

      // when data is not valid, continue
      if (!sampleInfo.valid_data) {
        if (sampleInfo.instance_state == InstanceStateKind.NOT_ALIVE_DISPOSED_INSTANCE_STATE
            && log.isInfoEnabled()) {
          log.info("Disposed: {}", sampleInfo.instance_handle.toString());
        }
        continue;
      }

      // print instance handle that was discovered
      if (log.isInfoEnabled()) {
        log.info("Discovered: {}", sampleInfo.instance_handle.toString());
      }
    }

    // return loan
    publicationDataReader.return_loan(publicationDataSeq, sampleInfoSeq);
  }
}
