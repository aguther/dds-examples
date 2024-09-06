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

import com.rti.connext.infrastructure.Sample;
import com.rti.connext.requestreply.SimpleReplierListener;
import idl.ReplyResult;
import idl.ReplyType;
import idl.RequestType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReplySender implements SimpleReplierListener<RequestType, ReplyType> {

  private static final Logger LOGGER = LogManager.getLogger(ReplySender.class);

  ReplySender() {
  }

  @Override
  public ReplyType onRequestAvailable(
    final Sample<RequestType> sample
  ) {
    // check if request is valid
    if (!sample.getInfo().valid_data) {
      LOGGER.error("Received request was not valid!");
      return null;
    }

    // log request
    RequestType request = sample.getData();
    LOGGER.info(
      "Received request (id='{}', request='{}')",
      request.id,
      request.request
    );

    // create reply
    ReplyType reply = new ReplyType();
    reply.result = ReplyResult.SUCCESS;
    reply.description = String.format("%d", request.id);

    // log reply
    LOGGER.info(
      "Writing reply (result='{}', description='{}')",
      reply.result,
      reply.description
    );

    // send reply
    return reply;
  }

  @Override
  public void returnLoan(
    final ReplyType replyType
  ) {
    // nothing to do
  }
}
