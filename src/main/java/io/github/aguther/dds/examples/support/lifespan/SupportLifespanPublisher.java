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

package io.github.aguther.dds.examples.support.lifespan;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.aguther.dds.logging.Slf4jDdsLogger;
import io.github.aguther.dds.support.publication.DataWriterAutomaticUnregisterDecorator;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.publication.DataWriter;
import idl.ShapeTypeExtended;
import idl.ShapeTypeExtendedTypeSupport;
import idl.ShapeTypeTypeSupport;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Option;

public class SupportLifespanPublisher extends AbstractExecutionThreadService implements
  Callable<SupportLifespanPublisher> {

  private static final Logger LOGGER = LogManager.getLogger(SupportLifespanPublisher.class);

  private static SupportLifespanPublisher serviceInstance;

  private DomainParticipant domainParticipant;
  private DataWriter dataWriterSquare;

  @Option(
    names = {"--sleepIntermediate"},
    defaultValue = "1000"
  )
  private int sleepTimeIntermediate;

  @Option(
    names = {"--sleepRoundTrip"},
    defaultValue = "5000"
  )
  private int sleepTimeRoundTrip;

  @Option(
    names = {"--plain"},
    defaultValue = "false"
  )
  private boolean usePlainDataWriter;

  public static void main(
    final String[] args
  ) {
    // create service
    serviceInstance = CommandLine.call(new SupportLifespanPublisher(), args);

    // check if service instance was created and exit if not
    if (serviceInstance == null) {
      System.exit(1);
    }

    // register shutdown hook
    registerShutdownHook();

    // start the service
    serviceInstance.startAsync();

    // wait for termination
    serviceInstance.awaitTerminated();

    // service terminated
    LOGGER.info("Service terminated");
  }

  @Override
  public SupportLifespanPublisher call() {
    return this;
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
      String.format("ShutdownHook-%s", SupportLifespanPublisher.class.getName())
    ));
  }

  @Override
  protected void startUp() {
    // log service start
    LOGGER.info("Service is starting");

    // startup DDS
    startupDds();

    // log service start
    LOGGER.info("Service start finished");
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
      "DomainParticipantLibrary::ShapeSupportPublisher"
    );
    checkNotNull(domainParticipant);

    // get data writer for square and decorate it with automatic unregister
    DataWriter dataWriter = domainParticipant.lookup_datawriter_by_name("SquarePublisher::SquareDataWriter");
    checkNotNull(dataWriter);
    dataWriterSquare = usePlainDataWriter ? dataWriter : new DataWriterAutomaticUnregisterDecorator(dataWriter);
    checkNotNull(dataWriterSquare);
  }

  @Override
  protected void run() throws InterruptedException {
    ShapeTypeExtended sampleSquare = new ShapeTypeExtended();
    sampleSquare.color = "SQUARE";

    while (isRunning()) {
      // publish first square sample
      sampleSquare.x = 1;
      dataWriterSquare.write_untyped(sampleSquare, InstanceHandle_t.HANDLE_NIL);

      // sleep intermediate
      Thread.sleep(sleepTimeIntermediate);

      // publish second square sample
      sampleSquare.x = 2;
      dataWriterSquare.write_untyped(sampleSquare, InstanceHandle_t.HANDLE_NIL);

      // sleep some time to trigger unregister
      Thread.sleep(sleepTimeRoundTrip);
    }
  }

  @Override
  protected void shutDown() {
    // log service start
    LOGGER.info("Service is shutting down");

    // shutdown DDS
    shutdownDds();

    // log service start
    LOGGER.info("Service shutdown finished");
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
