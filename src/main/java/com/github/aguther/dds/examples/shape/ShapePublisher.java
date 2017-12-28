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

package com.github.aguther.dds.examples.shape;

import com.github.aguther.dds.logging.Slf4jDdsLogger;
import com.google.common.util.concurrent.AbstractIdleService;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import idl.ShapeFillKind;
import idl.ShapeTypeExtendedTypeSupport;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShapePublisher extends AbstractIdleService {

  private static final Logger log;

  private static ShapePublisher serviceInstance;

  private static DomainParticipant domainParticipant;

  private static Thread publishThread;
  private static ShapeTypeExtendedPublisher shapeTypeExtendedPublisher;

  static {
    log = LoggerFactory.getLogger(ShapePublisher.class);
  }

  public static void main(
      String[] args
  ) {
    // register shutdown hook
    registerShutdownHook();

    // create service
    serviceInstance = new ShapePublisher();

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
        ShapeTypeExtendedTypeSupport.get_instance(),
        ShapeTypeExtendedTypeSupport.get_type_name()
    );

    // create participant from config
    domainParticipant = DomainParticipantFactory.get_instance().create_participant_from_config(
        "DomainParticipantLibrary::ShapePublisher"
    );
  }

  private static void startPublish() {
    // create initial attributes of shape
    ShapeAttributes shapeAttributes = new ShapeAttributes(
        "BLUE",
        30,
        ShapeFillKind.SOLID_FILL,
        0.0f
    );

    // create shape publisher
    shapeTypeExtendedPublisher = new ShapeTypeExtendedPublisher(
        shapeAttributes,
        domainParticipant,
        "Publisher::ShapeTypeExtendedDataWriter",
        50
    );

    // create and start thread
    publishThread = new Thread(shapeTypeExtendedPublisher);
    publishThread.start();
  }

  private static void stopPublish() {
    // check if we need to stop publish
    if (shapeTypeExtendedPublisher == null) {
      return;
    }

    // signal termination
    shapeTypeExtendedPublisher.stop();

    // wait for thread to finish
    try {
      publishThread.join();
    } catch (InterruptedException e) {
      log.error("Interrupted on join of publisher thread.", e);
      Thread.currentThread().interrupt();
    }

    // set objects to null
    publishThread = null;
    shapeTypeExtendedPublisher = null;
  }

  private static void shutdownDds() {
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
