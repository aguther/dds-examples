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

package com.github.aguther.dds.examples.prometheus.monitoring;

import com.github.aguther.dds.logging.Slf4jDdsLogger;
import com.github.aguther.dds.support.subscription.DataReaderWatcher;
import com.github.aguther.dds.support.subscription.OnDataAvailableListener;
import com.github.aguther.dds.support.subscription.SampleTaker;
import com.github.aguther.dds.support.subscription.SampleWithInfoCopier;
import com.google.common.util.concurrent.AbstractIdleService;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.ReadConditionParams;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.StreamKind;
import com.rti.dds.subscription.ViewStateKind;
import idl.rti.dds.monitoring.DataReaderDescription;
import idl.rti.dds.monitoring.DataReaderDescriptionSeq;
import idl.rti.dds.monitoring.DataReaderDescriptionTypeSupport;
import idl.rti.dds.monitoring.DataReaderEntityMatchedPublicationStatistics;
import idl.rti.dds.monitoring.DataReaderEntityMatchedPublicationStatisticsSeq;
import idl.rti.dds.monitoring.DataReaderEntityMatchedPublicationStatisticsTypeSupport;
import idl.rti.dds.monitoring.DataReaderEntityStatistics;
import idl.rti.dds.monitoring.DataReaderEntityStatisticsSeq;
import idl.rti.dds.monitoring.DataReaderEntityStatisticsTypeSupport;
import idl.rti.dds.monitoring.DataWriterDescription;
import idl.rti.dds.monitoring.DataWriterDescriptionSeq;
import idl.rti.dds.monitoring.DataWriterDescriptionTypeSupport;
import idl.rti.dds.monitoring.DataWriterEntityMatchedSubscriptionStatistics;
import idl.rti.dds.monitoring.DataWriterEntityMatchedSubscriptionStatisticsSeq;
import idl.rti.dds.monitoring.DataWriterEntityMatchedSubscriptionStatisticsTypeSupport;
import idl.rti.dds.monitoring.DataWriterEntityMatchedSubscriptionWithLocatorStatistics;
import idl.rti.dds.monitoring.DataWriterEntityMatchedSubscriptionWithLocatorStatisticsSeq;
import idl.rti.dds.monitoring.DataWriterEntityMatchedSubscriptionWithLocatorStatisticsTypeSupport;
import idl.rti.dds.monitoring.DataWriterEntityStatistics;
import idl.rti.dds.monitoring.DataWriterEntityStatisticsSeq;
import idl.rti.dds.monitoring.DataWriterEntityStatisticsTypeSupport;
import idl.rti.dds.monitoring.DomainParticipantDescription;
import idl.rti.dds.monitoring.DomainParticipantDescriptionSeq;
import idl.rti.dds.monitoring.DomainParticipantDescriptionTypeSupport;
import idl.rti.dds.monitoring.DomainParticipantEntityStatistics;
import idl.rti.dds.monitoring.DomainParticipantEntityStatisticsSeq;
import idl.rti.dds.monitoring.DomainParticipantEntityStatisticsTypeSupport;
import idl.rti.dds.monitoring.PublisherDescription;
import idl.rti.dds.monitoring.PublisherDescriptionSeq;
import idl.rti.dds.monitoring.PublisherDescriptionTypeSupport;
import idl.rti.dds.monitoring.SubscriberDescription;
import idl.rti.dds.monitoring.SubscriberDescriptionSeq;
import idl.rti.dds.monitoring.SubscriberDescriptionTypeSupport;
import idl.rti.dds.monitoring.TopicDescription;
import idl.rti.dds.monitoring.TopicDescriptionSeq;
import idl.rti.dds.monitoring.TopicDescriptionTypeSupport;
import idl.rti.dds.monitoring.TopicEntityStatistics;
import idl.rti.dds.monitoring.TopicEntityStatisticsSeq;
import idl.rti.dds.monitoring.TopicEntityStatisticsTypeSupport;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Collector extends AbstractIdleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(Collector.class);

  private static Collector serviceInstance;

  private DomainParticipant domainParticipant;

  private HTTPServer httpServer;

  private DataReaderWatcher dataReaderWatcherDomainParticipantEntityStatistics;

  private DataReaderWatcher dataReaderWatcherDomainParticipantDescription;
  private DataReaderWatcher dataReaderWatcherTopicDescription;
  private DataReaderWatcher dataReaderWatcherPublisherDescription;
  private DataReaderWatcher dataReaderWatcherDataWriterDescription;
  private DataReaderWatcher dataReaderWatcherSubscriberDescription;
  private DataReaderWatcher dataReaderWatcherDataReaderDescription;

  private DescriptionProcessorCache descriptionProcessorCache;
  private DomainParticipantMetricProcessor domainParticipantMetricProcessor;

  private DataReaderWatcher dataReaderWatcherTopicEntityStatistics;
  private TopicMetricsProcessor topicMetricsProcessor;

  private DataReaderWatcher dataReaderWatcherDataReaderEntityStatistics;
  private DataReaderMetricsProcessor dataReaderMetricsProcessor;

  private DataReaderWatcher dataReaderWatcherDataReaderEntityMatchedPublicationStatistics;
  private DataReaderMatchedPublicationMetricsProcessor dataReaderMatchedPublicationMetricsProcessor;

  private DataReaderWatcher dataReaderWatcherDataWriterEntityStatistics;
  private DataWriterMetricsProcessor dataWriterMetricsProcessor;

  private DataReaderWatcher dataReaderWatcherDataWriterEntityMatchedSubscriptionStatistics;
  private DataWriterMatchedSubscriptionMetricsProcessor dataWriterMatchedSubscriptionMetricsProcessor;

  private DataReaderWatcher dataReaderWatcherDataWriterEntityMatchedSubscriptionWithLocatorStatistics;
  private DataWriterMatchedSubscriptionWithLocatorMetricsProcessor dataWriterMatchedSubscriptionWithLocatorMetricsProcessor;

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

    // create description processor cache
    descriptionProcessorCache = new DescriptionProcessorCache();

    // create metrics processors
    domainParticipantMetricProcessor = new DomainParticipantMetricProcessor(
      descriptionProcessorCache);

    topicMetricsProcessor = new TopicMetricsProcessor(
      descriptionProcessorCache);

    dataReaderMetricsProcessor = new DataReaderMetricsProcessor(
      descriptionProcessorCache);

    dataReaderMatchedPublicationMetricsProcessor = new DataReaderMatchedPublicationMetricsProcessor(
      descriptionProcessorCache);

    dataWriterMetricsProcessor = new DataWriterMetricsProcessor(
      descriptionProcessorCache);

    dataWriterMatchedSubscriptionMetricsProcessor = new DataWriterMatchedSubscriptionMetricsProcessor(
      descriptionProcessorCache);

    dataWriterMatchedSubscriptionWithLocatorMetricsProcessor = new DataWriterMatchedSubscriptionWithLocatorMetricsProcessor(
      descriptionProcessorCache);

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
      DomainParticipantDescriptionTypeSupport.get_instance(),
      DomainParticipantDescriptionTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
      TopicDescriptionTypeSupport.get_instance(),
      TopicDescriptionTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
      SubscriberDescriptionTypeSupport.get_instance(),
      SubscriberDescriptionTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
      DataReaderDescriptionTypeSupport.get_instance(),
      DataReaderDescriptionTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
      PublisherDescriptionTypeSupport.get_instance(),
      PublisherDescriptionTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
      DataWriterDescriptionTypeSupport.get_instance(),
      DataWriterDescriptionTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
      DomainParticipantEntityStatisticsTypeSupport.get_instance(),
      DomainParticipantEntityStatisticsTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
      TopicEntityStatisticsTypeSupport.get_instance(),
      TopicEntityStatisticsTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
      DataReaderEntityStatisticsTypeSupport.get_instance(),
      DataReaderEntityStatisticsTypeSupport.get_type_name()
    );
    DomainParticipantFactory.get_instance().register_type_support(
      DataReaderEntityMatchedPublicationStatisticsTypeSupport.get_instance(),
      DataReaderEntityMatchedPublicationStatisticsTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
      DataWriterEntityStatisticsTypeSupport.get_instance(),
      DataWriterEntityStatisticsTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
      DataWriterEntityMatchedSubscriptionStatisticsTypeSupport.get_instance(),
      DataWriterEntityMatchedSubscriptionStatisticsTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
      DataWriterEntityMatchedSubscriptionWithLocatorStatisticsTypeSupport.get_instance(),
      DataWriterEntityMatchedSubscriptionWithLocatorStatisticsTypeSupport.get_type_name()
    );

    // create participant from config
    domainParticipant = DomainParticipantFactory.get_instance().create_participant_from_config(
      "DomainParticipantLibrary::DdsMonitoringLibraryPrometheus"
    );
  }

  private void startSubscription() {
    // start subscription
    ReadConditionParams readConditionParams = new ReadConditionParams();
    readConditionParams.stream_kinds = StreamKind.LIVE_STREAM;
    readConditionParams.instance_states = InstanceStateKind.ANY_INSTANCE_STATE;
    readConditionParams.view_states = ViewStateKind.ANY_VIEW_STATE;
    readConditionParams.sample_states = SampleStateKind.NOT_READ_SAMPLE_STATE;

    dataReaderWatcherDomainParticipantDescription = new DataReaderWatcher<>(
      domainParticipant.lookup_datareader_by_name("Subscriber::DomainParticipantDescription"),
      readConditionParams,
      new SampleTaker<>(new DomainParticipantDescriptionSeq()),
      new SampleWithInfoCopier<>(DomainParticipantDescription.class,
        (OnDataAvailableListener<DomainParticipantDescription>) (dataReader, sample, info) ->
          descriptionProcessorCache.process(sample, info)
      )
    );
    dataReaderWatcherTopicDescription = new DataReaderWatcher<>(
      domainParticipant.lookup_datareader_by_name("Subscriber::TopicDescription"),
      readConditionParams,
      new SampleTaker<>(new TopicDescriptionSeq()),
      new SampleWithInfoCopier<>(TopicDescription.class,
        (OnDataAvailableListener<TopicDescription>) (dataReader, sample, info) ->
          descriptionProcessorCache.process(sample, info)
      )
    );
    dataReaderWatcherPublisherDescription = new DataReaderWatcher<>(
      domainParticipant.lookup_datareader_by_name("Subscriber::PublisherDescription"),
      readConditionParams,
      new SampleTaker<>(new PublisherDescriptionSeq()),
      new SampleWithInfoCopier<>(PublisherDescription.class,
        (OnDataAvailableListener<PublisherDescription>) (dataReader, sample, info) ->
          descriptionProcessorCache.process(sample, info)
      )
    );
    dataReaderWatcherDataWriterDescription = new DataReaderWatcher<>(
      domainParticipant.lookup_datareader_by_name("Subscriber::DataWriterDescription"),
      readConditionParams,
      new SampleTaker<>(new DataWriterDescriptionSeq()),
      new SampleWithInfoCopier<>(DataWriterDescription.class,
        (OnDataAvailableListener<DataWriterDescription>) (dataReader, sample, info) ->
          descriptionProcessorCache.process(sample, info)
      )
    );
    dataReaderWatcherSubscriberDescription = new DataReaderWatcher<>(
      domainParticipant.lookup_datareader_by_name("Subscriber::SubscriberDescription"),
      readConditionParams,
      new SampleTaker<>(new SubscriberDescriptionSeq()),
      new SampleWithInfoCopier<>(SubscriberDescription.class,
        (OnDataAvailableListener<SubscriberDescription>) (dataReader, sample, info) ->
          descriptionProcessorCache.process(sample, info)
      )
    );
    dataReaderWatcherDataReaderDescription = new DataReaderWatcher<>(
      domainParticipant.lookup_datareader_by_name("Subscriber::DataReaderDescription"),
      readConditionParams,
      new SampleTaker<>(new DataReaderDescriptionSeq()),
      new SampleWithInfoCopier<>(DataReaderDescription.class,
        (OnDataAvailableListener<DataReaderDescription>) (dataReader, sample, info) ->
          descriptionProcessorCache.process(sample, info)
      )
    );

    dataReaderWatcherDomainParticipantEntityStatistics = new DataReaderWatcher<>(
      domainParticipant.lookup_datareader_by_name("Subscriber::DomainParticipantEntityStatistics"),
      readConditionParams,
      new SampleTaker<>(new DomainParticipantEntityStatisticsSeq()),
      (OnDataAvailableListener<DomainParticipantEntityStatistics>) (dataReader, sample, info) ->
        domainParticipantMetricProcessor.process(sample, info)
    );
    dataReaderWatcherTopicEntityStatistics = new DataReaderWatcher<>(
      domainParticipant.lookup_datareader_by_name("Subscriber::TopicEntityStatistics"),
      readConditionParams,
      new SampleTaker<>(new TopicEntityStatisticsSeq()),
      (OnDataAvailableListener<TopicEntityStatistics>) (dataReader, sample, info) ->
        topicMetricsProcessor.process(sample, info)
    );
    dataReaderWatcherDataReaderEntityStatistics = new DataReaderWatcher<>(
      domainParticipant.lookup_datareader_by_name("Subscriber::DataReaderEntityStatistics"),
      readConditionParams,
      new SampleTaker<>(new DataReaderEntityStatisticsSeq()),
      (OnDataAvailableListener<DataReaderEntityStatistics>) (dataReader, sample, info) ->
        dataReaderMetricsProcessor.process(sample, info)
    );
    dataReaderWatcherDataReaderEntityMatchedPublicationStatistics = new DataReaderWatcher<>(
      domainParticipant.lookup_datareader_by_name("Subscriber::DataReaderEntityMatchedPublicationStatistics"),
      readConditionParams,
      new SampleTaker<>(new DataReaderEntityMatchedPublicationStatisticsSeq()),
      (OnDataAvailableListener<DataReaderEntityMatchedPublicationStatistics>) (dataReader, sample, info) ->
        dataReaderMatchedPublicationMetricsProcessor.process(sample, info)
    );
    dataReaderWatcherDataWriterEntityStatistics = new DataReaderWatcher<>(
      domainParticipant.lookup_datareader_by_name("Subscriber::DataWriterEntityStatistics"),
      readConditionParams,
      new SampleTaker<>(new DataWriterEntityStatisticsSeq()),
      (OnDataAvailableListener<DataWriterEntityStatistics>) (dataReader, sample, info) ->
        dataWriterMetricsProcessor.process(sample, info)
    );
    dataReaderWatcherDataWriterEntityMatchedSubscriptionStatistics = new DataReaderWatcher<>(
      domainParticipant.lookup_datareader_by_name("Subscriber::DataWriterEntityMatchedSubscriptionStatistics"),
      readConditionParams,
      new SampleTaker<>(new DataWriterEntityMatchedSubscriptionStatisticsSeq()),
      (OnDataAvailableListener<DataWriterEntityMatchedSubscriptionStatistics>) (dataReader, sample, info) ->
        dataWriterMatchedSubscriptionMetricsProcessor.process(sample, info)
    );
    dataReaderWatcherDataWriterEntityMatchedSubscriptionWithLocatorStatistics = new DataReaderWatcher<>(
      domainParticipant
        .lookup_datareader_by_name("Subscriber::DataWriterEntityMatchedSubscriptionWithLocatorStatistics"),
      readConditionParams,
      new SampleTaker<>(new DataWriterEntityMatchedSubscriptionWithLocatorStatisticsSeq()),
      (OnDataAvailableListener<DataWriterEntityMatchedSubscriptionWithLocatorStatistics>) (dataReader, sample, info) ->
        dataWriterMatchedSubscriptionWithLocatorMetricsProcessor.process(sample, info)
    );
  }

  private void stopSubscription() {
    if (dataReaderWatcherDomainParticipantDescription != null) {
      dataReaderWatcherDomainParticipantDescription.close();
      dataReaderWatcherDomainParticipantDescription = null;
    }
    if (dataReaderWatcherTopicDescription != null) {
      dataReaderWatcherTopicDescription.close();
      dataReaderWatcherTopicDescription = null;
    }
    if (dataReaderWatcherPublisherDescription != null) {
      dataReaderWatcherPublisherDescription.close();
      dataReaderWatcherPublisherDescription = null;
    }
    if (dataReaderWatcherDataWriterDescription != null) {
      dataReaderWatcherDataWriterDescription.close();
      dataReaderWatcherDataWriterDescription = null;
    }
    if (dataReaderWatcherSubscriberDescription != null) {
      dataReaderWatcherSubscriberDescription.close();
      dataReaderWatcherSubscriberDescription = null;
    }
    if (dataReaderWatcherDataReaderDescription != null) {
      dataReaderWatcherDataReaderDescription.close();
      dataReaderWatcherDataReaderDescription = null;
    }

    if (dataReaderWatcherDomainParticipantEntityStatistics != null) {
      dataReaderWatcherDomainParticipantEntityStatistics.close();
      dataReaderWatcherDomainParticipantEntityStatistics = null;
    }
    if (dataReaderWatcherTopicEntityStatistics != null) {
      dataReaderWatcherTopicEntityStatistics.close();
      dataReaderWatcherTopicEntityStatistics = null;
    }
    if (dataReaderWatcherDataReaderEntityStatistics != null) {
      dataReaderWatcherDataReaderEntityStatistics.close();
      dataReaderWatcherDataReaderEntityStatistics = null;
    }
    if (dataReaderWatcherDataReaderEntityMatchedPublicationStatistics != null) {
      dataReaderWatcherDataReaderEntityMatchedPublicationStatistics.close();
      dataReaderWatcherDataReaderEntityMatchedPublicationStatistics = null;
    }
    if (dataReaderWatcherDataWriterEntityStatistics != null) {
      dataReaderWatcherDataWriterEntityStatistics.close();
      dataReaderWatcherDataWriterEntityStatistics = null;
    }
    if (dataReaderWatcherDataWriterEntityMatchedSubscriptionStatistics != null) {
      dataReaderWatcherDataWriterEntityMatchedSubscriptionStatistics.close();
      dataReaderWatcherDataWriterEntityMatchedSubscriptionStatistics = null;
    }
    if (dataReaderWatcherDataWriterEntityMatchedSubscriptionWithLocatorStatistics != null) {
      dataReaderWatcherDataWriterEntityMatchedSubscriptionWithLocatorStatistics.close();
      dataReaderWatcherDataWriterEntityMatchedSubscriptionWithLocatorStatistics = null;
    }

    if (descriptionProcessorCache != null) {
      descriptionProcessorCache = null;
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
