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

package com.github.aguther.dds.examples.requestreply;

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

  private static final Logger log;

  static {
    log = LoggerFactory.getLogger(RequestSender.class);
  }

  private boolean shouldTerminate;
  private Requester<RequestType, ReplyType> requester;
  private int replyWaitTime;
  private int sleepTime;

  private int count;

  RequestSender(
      Requester<RequestType, ReplyType> requester,
      int replyWaitTime,
      int sleepTime
  ) {
    if (requester == null) {
      throw new IllegalArgumentException("Requester must not be null!");
    }
    if (replyWaitTime < 0) {
      throw new IllegalArgumentException("ResponseWaitTime must be greater or equal to 0.");
    }
    if (sleepTime < 0) {
      throw new IllegalArgumentException("SleepTime must be greater or equal to 0.");
    }

    this.requester = requester;
    this.replyWaitTime = replyWaitTime;
    this.sleepTime = sleepTime;
  }

  void stop() {
    shouldTerminate = true;
  }

  @Override
  public void run() {
    log.info("Start sending ...");

    while (!shouldTerminate) {
      try {
        // create sample
        RequestType request = new RequestType();
        request.id = count++;
        request.request = RequestKind.START;

        // log request
        log.info(
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
            log.info(
                "Received reply (result='{}', description='{}')",
                reply.result,
                reply.description
            );
          } else {
            log.warn("Invalid reply received.");
          }
        } else {
          log.warn("No reply received.");
        }

        // wait some time
        if (sleepTime >= 0) {
          Thread.sleep(sleepTime);
        }

      } catch (RETCODE_ERROR e) {
        // log the problem and sTerminate the application
        log.error("Failed to write request.", e);
      } catch (InterruptedException e) {
        log.error("Failed to wait.", e);
        Thread.currentThread().interrupt();
      }
    }

    log.info("... done.");
  }
}
