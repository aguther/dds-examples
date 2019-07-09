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

package com.github.aguther.dds.examples.support.partition_groups;

import static com.google.common.base.Preconditions.checkNotNull;

import com.github.aguther.dds.logging.Slf4jDdsLogger;
import com.github.aguther.dds.support.DataReaderWatcher;
import com.github.aguther.dds.support.OnDataAvailableListener;
import com.github.aguther.dds.support.PartitionGroup;
import com.github.aguther.dds.support.PartitionGroupSubscriberAdapter;
import com.github.aguther.dds.support.SampleTaker;
import com.google.common.util.concurrent.AbstractIdleService;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.ReadConditionParams;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.StreamKind;
import com.rti.dds.subscription.ViewStateKind;
import idl.ShapeTypeExtended;
import idl.ShapeTypeExtendedSeq;
import idl.ShapeTypeExtendedTypeSupport;
import idl.ShapeTypeTypeSupport;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupportPartitionGroupsSubscriber extends AbstractIdleService implements
  OnDataAvailableListener<ShapeTypeExtended> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SupportPartitionGroupsSubscriber.class);

  private static SupportPartitionGroupsSubscriber serviceInstance;

  private DomainParticipant domainParticipant;

  private DataReader dataReaderSquare;
  private DataReaderWatcher dataReaderWatcherSquare;

  private DataReader dataReaderCircle;
  private DataReaderWatcher dataReaderWatcherCircle;

  private DataReader dataReaderTriangle;
  private DataReaderWatcher dataReaderWatcherTriangle;

  public static void main(
    final String[] args
  ) {
    // register shutdown hook
    registerShutdownHook();

    // create service
    serviceInstance = new SupportPartitionGroupsSubscriber();

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
      String.format("ShutdownHook-%s", SupportPartitionGroupsSubscriber.class.getName())
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

    // get data reader for circle
    dataReaderCircle = domainParticipant.lookup_datareader_by_name("CircleSubscriber::CircleDataReader");
    checkNotNull(dataReaderCircle);

    // get data reader for triangle
    dataReaderTriangle = domainParticipant.lookup_datareader_by_name("TriangleSubscriber::TriangleDataReader");
    checkNotNull(dataReaderTriangle);

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
      this
    );

    // create data reader watcher for square
    dataReaderWatcherCircle = new DataReaderWatcher<>(
      dataReaderCircle,
      readConditionParams,
      new SampleTaker<>(new ShapeTypeExtendedSeq()),
      this
    );

    // create data reader watcher for square
    dataReaderWatcherTriangle = new DataReaderWatcher<>(
      dataReaderTriangle,
      readConditionParams,
      new SampleTaker<>(new ShapeTypeExtendedSeq()),
      this
    );

    // create partition group
    PartitionGroup partitionGroup = new PartitionGroup(
      new PartitionGroupSubscriberAdapter(domainParticipant.lookup_subscriber_by_name("SquareSubscriber")),
      new PartitionGroupSubscriberAdapter(domainParticipant.lookup_subscriber_by_name("CircleSubscriber"))
    );
    partitionGroup.addPartitions("2");
  }

  @Override
  public void onDataAvailable(
    DataReader dataReader,
    ShapeTypeExtended sample,
    SampleInfo info
  ) {
    if (info.valid_data) {
      LOGGER.info("received: {}", sample.color);
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
    // delete data reader watchers
    if (dataReaderSquare != null) {
      dataReaderWatcherSquare.close();
      dataReaderWatcherSquare = null;
    }
    if (dataReaderCircle != null) {
      dataReaderWatcherCircle.close();
      dataReaderWatcherCircle = null;
    }
    if (dataReaderTriangle != null) {
      dataReaderWatcherTriangle.close();
      dataReaderWatcherTriangle = null;
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
