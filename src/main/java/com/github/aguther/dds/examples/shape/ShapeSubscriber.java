/*
 * MIT License
 *
 * Copyright (c) 2018 Andreas Guther
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

package com.github.aguther.dds.examples.shape;

import com.github.aguther.dds.logging.Slf4jDdsLogger;
import com.google.common.util.concurrent.AbstractIdleService;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import idl.ShapeTypeExtendedTypeSupport;
import idl.ShapeTypeTypeSupport;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShapeSubscriber extends AbstractIdleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ShapePublisher.class);

  private static ShapeSubscriber serviceInstance;

  private DomainParticipant domainParticipant;
  private ShapeTypeExtendedListener shapeTypeExtendedListener;

  public static void main(
      final String[] args
  ) {
    // register shutdown hook
    registerShutdownHook();

    // create service
    serviceInstance = new ShapeSubscriber();

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
        String.format("ShutdownHook-%s", ShapeSubscriber.class.getName())
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
        ShapeTypeExtendedTypeSupport.get_instance(),
        ShapeTypeTypeSupport.get_type_name()
    );

    // create participant from config
    domainParticipant = DomainParticipantFactory.get_instance().create_participant_from_config(
        "DomainParticipantLibrary::ShapeSubscriber"
    );
  }

  private void startSubscription() {
    // start subscription
    shapeTypeExtendedListener = new ShapeTypeExtendedListener(
        domainParticipant.lookup_datareader_by_name("Subscriber::ShapeTypeExtendedDataReader")
    );
  }

  private void stopSubscription() {
    // signal termination
    shapeTypeExtendedListener.stop();

    // set objects to null
    shapeTypeExtendedListener = null;
  }

  private void shutdownDds() {
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
