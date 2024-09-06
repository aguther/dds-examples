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
import com.google.common.util.concurrent.AbstractIdleService;
import com.rti.connext.requestreply.SimpleReplier;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import idl.ReplyType;
import idl.ReplyTypeTypeSupport;
import idl.RequestType;
import idl.RequestTypeTypeSupport;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reply extends AbstractIdleService {

  private static final Logger LOGGER = LogManager.getLogger(Reply.class);

  private static Reply serviceInstance;

  private DomainParticipant domainParticipant;
  private SimpleReplier<RequestType, ReplyType> replier;
  private ReplySender replySender;

  public static void main(
    final String[] args
  ) {
    // register shutdown hook
    registerShutdownHook();

    // create service
    serviceInstance = new Reply();

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
      String.format("ShutdownHook-%s", Reply.class.getName())
    ));
  }

  @Override
  protected void startUp() throws Exception {
    // log service start
    LOGGER.info("Service is starting");

    // startup DDS
    startupDds();

    // start publishing
    startSubscription();

    // log service start
    LOGGER.info("Service start finished");
  }

  @Override
  protected void shutDown() throws Exception {
    // log service start
    LOGGER.info("Service is shutting down");

    // stop publish
    stopSubscription();

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

  private void startSubscription() {
    // create reply sender
    replySender = new ReplySender();

    // create replier (currently not possible to use xml for the creation)
    replier = new SimpleReplier<>(
      domainParticipant,
      "RequestReply",
      replySender,
      RequestTypeTypeSupport.get_instance(),
      ReplyTypeTypeSupport.get_instance()
    );
  }

  private void stopSubscription() {
    // close and delete replier
    if (replier != null) {
      replier.close();
      replier = null;
    }

    // delete reply sender
    replySender = null;
  }

  private void shutdownDds() {
    // delete replier (if not already done)
    if (replier != null) {
      replier.close();
      replier = null;
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
