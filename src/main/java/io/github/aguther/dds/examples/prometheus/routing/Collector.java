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

package io.github.aguther.dds.examples.prometheus.routing;

import io.github.aguther.dds.examples.prometheus.routing.processors.ConfigEventProcessorCache;
import io.github.aguther.dds.examples.prometheus.routing.processors.PeriodicProcessor;
import io.github.aguther.dds.logging.Slf4jDdsLogger;
import io.github.aguther.dds.support.subscription.DataReaderWatcher;
import io.github.aguther.dds.support.subscription.SampleTaker;
import io.github.aguther.dds.support.subscription.SampleWithInfoCopier;
import com.google.common.util.concurrent.AbstractIdleService;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.ReadConditionParams;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.StreamKind;
import com.rti.dds.subscription.ViewStateKind;
import idl.RTI.Service.Monitoring.Config;
import idl.RTI.Service.Monitoring.ConfigSeq;
import idl.RTI.Service.Monitoring.ConfigTypeSupport;
import idl.RTI.Service.Monitoring.Event;
import idl.RTI.Service.Monitoring.EventSeq;
import idl.RTI.Service.Monitoring.EventTypeSupport;
import idl.RTI.Service.Monitoring.Periodic;
import idl.RTI.Service.Monitoring.PeriodicSeq;
import idl.RTI.Service.Monitoring.PeriodicTypeSupport;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Collector extends AbstractIdleService {

  private static final Logger LOGGER = LogManager.getLogger(
    Collector.class);

  private static Collector serviceInstance;

  private DomainParticipant domainParticipant;

  private HTTPServer httpServer;

  private DataReaderWatcher<Config> dataReaderWatcherConfig;
  private DataReaderWatcher<Event> dataReaderWatcherEvent;
  private DataReaderWatcher<Periodic> dataReaderWatcherPeriodic;

  private ConfigEventProcessorCache configEventProcessorCache;
  private PeriodicProcessor periodicProcessor;

  public static void main(
    final String[] args
  ) {
    // register shutdown hook
    registerShutdownHook();

    // create service
    serviceInstance = new Collector();

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
      String.format("ShutdownHook-%s", Collector.class.getName())
    ));
  }

  @Override
  protected void startUp() throws IOException {
    // log service start
    LOGGER.info("Service is starting");

    // start http server
    httpServer = new HTTPServer(9102);

    // create metrics processors
    configEventProcessorCache = new ConfigEventProcessorCache();
    periodicProcessor = new PeriodicProcessor(configEventProcessorCache);

    // startup DDS
    startupDds();

    // start publishing
    startSubscription();

    // log service start
    LOGGER.info("Service start finished");
  }

  @Override
  protected void shutDown() {
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
      ConfigTypeSupport.get_instance(),
      ConfigTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
      EventTypeSupport.get_instance(),
      EventTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
      PeriodicTypeSupport.get_instance(),
      PeriodicTypeSupport.get_type_name()
    );

    // create participant from config
    domainParticipant = DomainParticipantFactory.get_instance().create_participant_from_config(
      "DomainParticipantLibrary::PrometheusRoutingCollector"
    );
  }

  private void startSubscription() {
    // start subscription
    ReadConditionParams readConditionParams = new ReadConditionParams();
    readConditionParams.stream_kinds = StreamKind.LIVE_STREAM;
    readConditionParams.instance_states = InstanceStateKind.ANY_INSTANCE_STATE;
    readConditionParams.view_states = ViewStateKind.ANY_VIEW_STATE;
    readConditionParams.sample_states = SampleStateKind.NOT_READ_SAMPLE_STATE;

    dataReaderWatcherConfig = new DataReaderWatcher<>(
      domainParticipant.lookup_datareader_by_name("Subscriber::Config"),
      readConditionParams,
      new SampleTaker<>(new ConfigSeq()),
      new SampleWithInfoCopier<>(Config.class,
        (dataReader, sample, info) ->
          configEventProcessorCache.process(sample, info)
      )
    );
    dataReaderWatcherEvent = new DataReaderWatcher<>(
      domainParticipant.lookup_datareader_by_name("Subscriber::Event"),
      readConditionParams,
      new SampleTaker<>(new EventSeq()),
      new SampleWithInfoCopier<>(Event.class,
        (dataReader, sample, info) ->
          configEventProcessorCache.process(sample, info)
      )
    );
    dataReaderWatcherPeriodic = new DataReaderWatcher<>(
      domainParticipant.lookup_datareader_by_name("Subscriber::Periodic"),
      readConditionParams,
      new SampleTaker<>(new PeriodicSeq()),
      new SampleWithInfoCopier<>(Periodic.class,
        (dataReader, sample, info) ->
          periodicProcessor.process(sample, info)
      )
    );
  }

  private void stopSubscription() {
    if (dataReaderWatcherConfig != null) {
      dataReaderWatcherConfig.close();
      dataReaderWatcherConfig = null;
    }
    if (dataReaderWatcherEvent != null) {
      dataReaderWatcherEvent.close();
      dataReaderWatcherEvent = null;
    }
    if (dataReaderWatcherPeriodic != null) {
      dataReaderWatcherPeriodic.close();
      dataReaderWatcherPeriodic = null;
    }

    if (httpServer != null) {
      httpServer.stop();
      httpServer = null;
    }
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
