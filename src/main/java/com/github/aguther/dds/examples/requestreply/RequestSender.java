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

package com.github.aguther.dds.examples.requestreply;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.rti.connext.infrastructure.Sample;
import com.rti.connext.requestreply.Requester;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import idl.ReplyType;
import idl.RequestKind;
import idl.RequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestSender implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestSender.class);

  private final Requester<RequestType, ReplyType> requester;
  private final int replyWaitTime;
  private final int sleepTime;

  private boolean shouldTerminate;
  private int count;

  RequestSender(
    final Requester<RequestType, ReplyType> requester,
    final int replyWaitTime,
    final int sleepTime
  ) {
    checkNotNull(requester, "Requester must not be null");
    checkArgument(replyWaitTime >= 0, "Time to wait for reply must be 0 or greater");
    checkArgument(sleepTime >= 0, "Time to sleep must be 0 or greater");

    this.requester = requester;
    this.replyWaitTime = replyWaitTime;
    this.sleepTime = sleepTime;
  }

  void stop() {
    shouldTerminate = true;
  }

  @Override
  public void run() {
    LOGGER.info("Start sending ...");

    while (!shouldTerminate) {
      try {
        // create sample
        RequestType request = new RequestType();
        request.id = count++;
        request.request = RequestKind.START;

        // log request
        LOGGER.info(
          "Writing request (id='{}', request='{}')",
          request.id,
          request.request
        );

        // write request
        requester.sendRequest(request);

        // wait for response
        requester.waitForReplies(Duration_t.from_millis(replyWaitTime));

        // create response entities
        Sample<ReplyType> sampleReply = requester.createReplySample();

        // receive reply
        boolean received = requester.receiveReply(
          sampleReply,
          Duration_t.from_millis(replyWaitTime)
        );

        // check reply
        if (received) {
          if (sampleReply.getInfo().valid_data) {
            // get response data
            ReplyType reply = sampleReply.getData();
            // log info
            LOGGER.info(
              "Received reply (result='{}', description='{}')",
              reply.result,
              reply.description
            );
          } else {
            LOGGER.warn("Invalid reply received.");
          }
        } else {
          LOGGER.warn("No reply received.");
        }

        // wait some time
        if (sleepTime >= 0) {
          Thread.sleep(sleepTime);
        }

      } catch (RETCODE_ERROR e) {
        // log the problem and sTerminate the application
        LOGGER.error("Failed to write request.", e);
      } catch (InterruptedException e) {
        LOGGER.error("Failed to wait.", e);
        Thread.currentThread().interrupt();
      }
    }

    LOGGER.info("... done.");
  }
}
