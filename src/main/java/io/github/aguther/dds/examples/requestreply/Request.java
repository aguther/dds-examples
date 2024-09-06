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

package io.github.aguther.dds.examples.requestreply;

import io.github.aguther.dds.logging.Slf4jDdsLogger;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.rti.connext.requestreply.Requester;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import idl.ReplyType;
import idl.ReplyTypeTypeSupport;
import idl.RequestType;
import idl.RequestTypeTypeSupport;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Request extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LogManager.getLogger(Request.class);

  private static Request serviceInstance;

  private DomainParticipant domainParticipant;
  private Requester<RequestType, ReplyType> requester;
  private RequestSender requestSender;

  public static void main(
    final String[] args
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
    LOGGER.info("Service terminated");
  }

  private static void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(
      () -> {
        LOGGER.info("Shutdown signal received");
        if (serviceInstance != null) {
          serviceInstance.stopAsync();
          serviceInstance.awaitTerminated();
        }
        LOGGER.info("Shutdown signal finished");
      },
      String.format("ShutdownHook-%s", Request.class.getName())
    ));
  }

  @Override
  protected void startUp() throws Exception {
    // log service start
    LOGGER.info("Service is starting");

    // startup DDS
    startupDds();

    // start publishing
    startPublish();

    // log service start
    LOGGER.info("Service start finished");
  }

  @Override
  protected void run() throws Exception {
    requestSender.run();
  }

  @Override
  protected void triggerShutdown() {
    // stop publish
    stopPublish();
  }

  @Override
  protected void shutDown() throws Exception {
    // log service start
    LOGGER.info("Service is shutting down");

    // shutdown DDS
    shutdownDds();

    // log service start
    LOGGER.info("Service shutdown finished");
  }

  private void startupDds() {
    // register logger DDS messages
    try {
      Slf4jDdsLogger.createRegisterLogger();
    } catch (IOException e) {
      LOGGER.error("Failed to create and register DDS logging device.", e);
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

  private void startPublish() {
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
  }

  private void stopPublish() {
    // check if we need to stop publish
    if (requestSender != null) {
      requestSender.stop();
    }
  }

  private void shutdownDds() {
    // delete requester (if not already deleted)
    if (requester != null) {
      requester.close();
      requester = null;
    }

    // delete domain participant
    if (domainParticipant != null) {
      domainParticipant.delete_contained_entities();
      DomainParticipantFactory.get_instance().delete_participant(domainParticipant);
      domainParticipant = null;
    }

    // finalize factory
    DomainParticipantFactory.finalize_instance();
  }
}
