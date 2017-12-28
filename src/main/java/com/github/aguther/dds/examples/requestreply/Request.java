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

import com.github.aguther.dds.util.Slf4jDdsLogger;
import com.google.common.util.concurrent.AbstractIdleService;
import com.rti.connext.requestreply.Requester;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import idl.ReplyType;
import idl.ReplyTypeTypeSupport;
import idl.RequestType;
import idl.RequestTypeTypeSupport;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Request extends AbstractIdleService {

  private static final Logger log;

  private static Request serviceInstance;

  private static DomainParticipant domainParticipant;
  private static Requester<RequestType, ReplyType> requester;

  private static Thread requestSenderThread;
  private static RequestSender requestSender;

  static {
    log = LoggerFactory.getLogger(Request.class);
  }

  public static void main(
      String[] args
  ) {
    // register shutdown hook
    registerShutdownHook();

    // create service
    serviceInstance = new Request();

    // start the service
    serviceInstance.startAsync();

    // wait for termination
    serviceInstance.awaitTerminated();

    // service terminated
    log.info("Service terminated");
  }

  private static void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Shutdown signal received");
      if (serviceInstance != null) {
        serviceInstance.stopAsync();
        serviceInstance.awaitTerminated();
      }
      log.info("Shutdown signal finished");
    }));
  }

  @Override
  protected void startUp() throws Exception {
    // log service start
    log.info("Service is starting");

    // startup DDS
    startupDds();

    // start publishing
    startPublish();

    // log service start
    log.info("Service start finished");
  }

  @Override
  protected void shutDown() throws Exception {
    // log service start
    log.info("Service is shutting down");

    // stop publish
    stopPublish();

    // shutdown DDS
    shutdownDds();

    // log service start
    log.info("Service shutdown finished");
  }

  private static void startupDds() {
    // register logger DDS messages
    try {
      Slf4jDdsLogger.createRegisterLogger();
    } catch (IOException e) {
      log.error("Failed to create and register DDS logging device.", e);
      return;
    }

    // register all types needed (this must be done before creation of the domain participant)
    DomainParticipantFactory.get_instance().register_type_support(
        RequestTypeTypeSupport.get_instance(),
        RequestTypeTypeSupport.get_type_name()
    );
    DomainParticipantFactory.get_instance().register_type_support(
        ReplyTypeTypeSupport.get_instance(),
        ReplyTypeTypeSupport.get_type_name()
    );

    // create participant from config
    domainParticipant = DomainParticipantFactory.get_instance().create_participant_from_config(
        "DomainParticipantLibrary::RequestReplyRequester"
    );
  }

  private static void startPublish() {
    // create requester (currently not possible to use xml for the creation)
    requester = new Requester<>(
        domainParticipant,
        "RequestReply",
        RequestTypeTypeSupport.get_instance(),
        ReplyTypeTypeSupport.get_instance()
    );

    // create sender
    requestSender = new RequestSender(
        requester,
        1000,
        1000
    );

    // create and start thread
    requestSenderThread = new Thread(requestSender);
    requestSenderThread.start();
  }

  private static void stopPublish() {
    // check if we need to stop publish
    if (requestSender == null) {
      return;
    }

    // signal termination
    requestSender.stop();

    // wait for thread to finish
    try {
      requestSenderThread.join();
    } catch (InterruptedException e) {
      log.error("Interrupted on join of sender thread.", e);
      Thread.currentThread().interrupt();
    }

    // set objects to null
    requestSenderThread = null;
    requestSender = null;

    // delete requester
    if (requester != null) {
      requester.close();
      requester = null;
    }
  }

  private static void shutdownDds() {
    // delete requester (if not already deleted)
    if (requester != null) {
      requester.close();
      requester = null;
    }

    // delete domain participant
    if (domainParticipant != null) {
      DomainParticipantFactory.get_instance().delete_participant(domainParticipant);
      domainParticipant = null;
    }

    // finalize factory
    DomainParticipantFactory.finalize_instance();
  }
}
