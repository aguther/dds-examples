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
import io.github.aguther.dds.support.subscription.CrudListener;
import io.github.aguther.dds.support.subscription.CrudSelectorNotAliveNoWriters;
import io.github.aguther.dds.support.subscription.DataReaderWatcher;
import io.github.aguther.dds.support.subscription.SampleInterpreterCrud;
import io.github.aguther.dds.support.subscription.SampleTaker;
import com.google.common.util.concurrent.AbstractIdleService;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.ReadConditionParams;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.StreamKind;
import com.rti.dds.subscription.ViewStateKind;
import idl.ShapeTypeExtended;
import idl.ShapeTypeExtendedSeq;
import idl.ShapeTypeExtendedTypeSupport;
import idl.ShapeTypeTypeSupport;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SupportLifespanSubscriber extends AbstractIdleService implements CrudListener<ShapeTypeExtended> {

  private static final Logger LOGGER = LogManager.getLogger(SupportLifespanSubscriber.class);

  private static SupportLifespanSubscriber serviceInstance;

  private DomainParticipant domainParticipant;

  private DataReader dataReaderSquare;
  private DataReaderWatcher dataReaderWatcherSquare;

  public static void main(
    final String[] args
  ) {
    // register shutdown hook
    registerShutdownHook();

    // create service
    serviceInstance = new SupportLifespanSubscriber();

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
      String.format("ShutdownHook-%s", SupportLifespanSubscriber.class.getName())
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
      "DomainParticipantLibrary::ShapeSupportSubscriber"
    );
    checkNotNull(domainParticipant);

    // get data reader for square
    dataReaderSquare = domainParticipant.lookup_datareader_by_name("SquareSubscriber::SquareDataReader");
    checkNotNull(dataReaderSquare);

    // start subscription
    ReadConditionParams readConditionParams = new ReadConditionParams();
    readConditionParams.stream_kinds = StreamKind.LIVE_STREAM;
    readConditionParams.instance_states = InstanceStateKind.ANY_INSTANCE_STATE;
    readConditionParams.view_states = ViewStateKind.ANY_VIEW_STATE;
    readConditionParams.sample_states = SampleStateKind.NOT_READ_SAMPLE_STATE;

    // create data reader watcher for square
    dataReaderWatcherSquare = new DataReaderWatcher<>(
      dataReaderSquare,
      readConditionParams,
      new SampleTaker<>(new ShapeTypeExtendedSeq()),
      new SampleInterpreterCrud<>(new CrudSelectorNotAliveNoWriters(), this)
    );
  }

  @Override
  public void add(
    ShapeTypeExtended sample
  ) {
    LOGGER.info("add: {} ({})", sample.color, sample.x);
  }

  @Override
  public void modify(
    ShapeTypeExtended sample
  ) {
    LOGGER.info("modify: {} ({})", sample.color, sample.x);
  }

  @Override
  public void delete(
    ShapeTypeExtended sample
  ) {
    LOGGER.info("delete: {} ({})", sample.color, sample.x);
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
    // delete data reader watcher
    if (dataReaderSquare != null) {
      dataReaderWatcherSquare.close();
      dataReaderWatcherSquare = null;
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
