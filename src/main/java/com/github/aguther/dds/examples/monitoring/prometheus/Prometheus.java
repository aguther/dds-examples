package com.github.aguther.dds.examples.monitoring.prometheus;

import com.github.aguther.dds.logging.Slf4jDdsLogger;
import com.github.aguther.dds.util.DataReaderWatcher;
import com.github.aguther.dds.util.DataReaderWatcherListener;
import com.github.aguther.dds.util.SampleTaker;
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

public class Prometheus extends AbstractIdleService {

  private static final Logger LOGGER = LoggerFactory.getLogger(Prometheus.class);

  private static Prometheus serviceInstance;

  private DomainParticipant domainParticipant;

  private HTTPServer httpServer;

  private DataReaderWatcher dataReaderWatcherDomainParticipantDescription;
  private DomainParticipantDescriptionMetricProcessor domainParticipantDescriptionMetricProcessor;

  private DataReaderWatcher dataReaderWatcherDomainParticipantEntityStatistics;
  private DomainParticipantEntityStatisticsMetricProcessor domainParticipantEntityStatisticsMetricProcessor;

  private DataReaderWatcher dataReaderWatcherTopicDescription;
  private TopicDescriptionMetricProcessor topicDescriptionMetricProcessor;

  private DataReaderWatcher dataReaderWatcherTopicEntityStatistics;
  private TopicEntityStatisticsMetricProcessor topicEntityStatisticsMetricProcessor;

  private DataReaderWatcher dataReaderWatcherPublisherDescription;
  private PublisherDescriptionMetricProcessor publisherDescriptionMetricProcessor;

  private DataReaderWatcher dataReaderWatcherSubscriberDescription;
  private SubscriberDescriptionMetricProcessor subscriberDescriptionMetricProcessor;

  private DataReaderWatcher dataReaderWatcherDataReaderDescription;
  private DataReaderDescriptionMetricProcessor dataReaderDescriptionMetricProcessor;

  private DataReaderWatcher dataReaderWatcherDataReaderEntityStatistics;
  private DataReaderEntityStatisticsMetricProcessor dataReaderEntityStatisticsMetricProcessor;

  private DataReaderWatcher dataReaderWatcherDataReaderEntityMatchedPublicationStatistics;
  private DataReaderEntityMatchedPublicationStatisticsMetricProcessor dataReaderEntityMatchedPublicationStatisticsMetricProcessor;

  private DataReaderWatcher dataReaderWatcherDataWriterDescription;
  private DataWriterDescriptionMetricProcessor dataWriterDescriptionMetricProcessor;

  private DataReaderWatcher dataReaderWatcherDataWriterEntityStatistics;
  private DataWriterEntityStatisticsMetricProcessor dataWriterEntityStatisticsMetricProcessor;

  private DataReaderWatcher dataReaderWatcherDataWriterEntityMatchedSubscriptionStatistics;
  private DataWriterEntityMatchedSubscriptionStatisticsMetricProcessor dataWriterEntityMatchedSubscriptionStatisticsMetricProcessor;

  private DataReaderWatcher dataReaderWatcherDataWriterEntityMatchedSubscriptionWithLocatorStatistics;
  private DataWriterEntityMatchedSubscriptionWithLocatorStatisticsMetricProcessor dataWriterEntityMatchedSubscriptionWithLocatorStatisticsMetricProcessor;

  public static void main(
      final String[] args
  ) {
    // register shutdown hook
    registerShutdownHook();

    // create service
    serviceInstance = new Prometheus();

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
        String.format("ShutdownHook-%s", Prometheus.class.getName())
    ));
  }

  @Override
  protected void startUp() throws IOException {
    // log service start
    LOGGER.info("Service is starting");

    // start http server
    httpServer = new HTTPServer(9091);

    // create metrics processors
    domainParticipantDescriptionMetricProcessor = new DomainParticipantDescriptionMetricProcessor();
    domainParticipantEntityStatisticsMetricProcessor = new DomainParticipantEntityStatisticsMetricProcessor();
    topicDescriptionMetricProcessor = new TopicDescriptionMetricProcessor();
    topicEntityStatisticsMetricProcessor = new TopicEntityStatisticsMetricProcessor();
    publisherDescriptionMetricProcessor = new PublisherDescriptionMetricProcessor();
    subscriberDescriptionMetricProcessor = new SubscriberDescriptionMetricProcessor();
    dataReaderDescriptionMetricProcessor = new DataReaderDescriptionMetricProcessor();
    dataReaderEntityStatisticsMetricProcessor = new DataReaderEntityStatisticsMetricProcessor();
    dataReaderEntityMatchedPublicationStatisticsMetricProcessor = new DataReaderEntityMatchedPublicationStatisticsMetricProcessor();
    dataWriterDescriptionMetricProcessor = new DataWriterDescriptionMetricProcessor();
    dataWriterEntityStatisticsMetricProcessor = new DataWriterEntityStatisticsMetricProcessor();
    dataWriterEntityMatchedSubscriptionStatisticsMetricProcessor = new DataWriterEntityMatchedSubscriptionStatisticsMetricProcessor();
    dataWriterEntityMatchedSubscriptionWithLocatorStatisticsMetricProcessor = new DataWriterEntityMatchedSubscriptionWithLocatorStatisticsMetricProcessor();

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
        DataReaderDescriptionTypeSupport.get_instance(),
        DataReaderDescriptionTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
        DataReaderEntityMatchedPublicationStatisticsTypeSupport.get_instance(),
        DataReaderEntityMatchedPublicationStatisticsTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
        DataReaderEntityStatisticsTypeSupport.get_instance(),
        DataReaderEntityStatisticsTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
        DataWriterDescriptionTypeSupport.get_instance(),
        DataWriterDescriptionTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
        DataWriterEntityMatchedSubscriptionStatisticsTypeSupport.get_instance(),
        DataWriterEntityMatchedSubscriptionStatisticsTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
        DataWriterEntityMatchedSubscriptionWithLocatorStatisticsTypeSupport.get_instance(),
        DataWriterEntityMatchedSubscriptionWithLocatorStatisticsTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
        DataWriterEntityStatisticsTypeSupport.get_instance(),
        DataWriterEntityStatisticsTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
        DomainParticipantDescriptionTypeSupport.get_instance(),
        DomainParticipantDescriptionTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
        DomainParticipantEntityStatisticsTypeSupport.get_instance(),
        DomainParticipantEntityStatisticsTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
        PublisherDescriptionTypeSupport.get_instance(),
        PublisherDescriptionTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
        SubscriberDescriptionTypeSupport.get_instance(),
        SubscriberDescriptionTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
        TopicDescriptionTypeSupport.get_instance(),
        TopicDescriptionTypeSupport.get_type_name()
    );

    DomainParticipantFactory.get_instance().register_type_support(
        TopicEntityStatisticsTypeSupport.get_instance(),
        TopicEntityStatisticsTypeSupport.get_type_name()
    );

    // create participant from config
    domainParticipant = DomainParticipantFactory.get_instance().create_participant_from_config(
        "DomainParticipantLibrary::Prometheus"
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
        (DataReaderWatcherListener<DomainParticipantDescription>) (sample, info) ->
            domainParticipantDescriptionMetricProcessor.process(sample, info)
    );

    dataReaderWatcherDomainParticipantEntityStatistics = new DataReaderWatcher<>(
        domainParticipant.lookup_datareader_by_name("Subscriber::DomainParticipantEntityStatistics"),
        readConditionParams,
        new SampleTaker<>(new DomainParticipantEntityStatisticsSeq()),
        (DataReaderWatcherListener<DomainParticipantEntityStatistics>) (sample, info) ->
            domainParticipantEntityStatisticsMetricProcessor.process(sample, info)
    );

    dataReaderWatcherTopicDescription = new DataReaderWatcher<>(
        domainParticipant.lookup_datareader_by_name("Subscriber::TopicDescription"),
        readConditionParams,
        new SampleTaker<>(new TopicDescriptionSeq()),
        (DataReaderWatcherListener<TopicDescription>) (sample, info) ->
            topicDescriptionMetricProcessor.process(sample, info)
    );

    dataReaderWatcherTopicEntityStatistics = new DataReaderWatcher<>(
        domainParticipant.lookup_datareader_by_name("Subscriber::TopicEntityStatistics"),
        readConditionParams,
        new SampleTaker<>(new TopicEntityStatisticsSeq()),
        (DataReaderWatcherListener<TopicEntityStatistics>) (sample, info) ->
            topicEntityStatisticsMetricProcessor.process(sample, info)
    );

    dataReaderWatcherPublisherDescription = new DataReaderWatcher<>(
        domainParticipant.lookup_datareader_by_name("Subscriber::PublisherDescription"),
        readConditionParams,
        new SampleTaker<>(new PublisherDescriptionSeq()),
        (DataReaderWatcherListener<PublisherDescription>) (sample, info) ->
            publisherDescriptionMetricProcessor.process(sample, info)
    );

    dataReaderWatcherSubscriberDescription = new DataReaderWatcher<>(
        domainParticipant.lookup_datareader_by_name("Subscriber::SubscriberDescription"),
        readConditionParams,
        new SampleTaker<>(new SubscriberDescriptionSeq()),
        (DataReaderWatcherListener<SubscriberDescription>) (sample, info) ->
            subscriberDescriptionMetricProcessor.process(sample, info)
    );

    dataReaderWatcherDataReaderDescription = new DataReaderWatcher<>(
        domainParticipant.lookup_datareader_by_name("Subscriber::DataReaderDescription"),
        readConditionParams,
        new SampleTaker<>(new DataReaderDescriptionSeq()),
        (DataReaderWatcherListener<DataReaderDescription>) (sample, info) ->
            dataReaderDescriptionMetricProcessor.process(sample, info)
    );

    dataReaderWatcherDataReaderEntityStatistics = new DataReaderWatcher<>(
        domainParticipant.lookup_datareader_by_name("Subscriber::DataReaderEntityStatistics"),
        readConditionParams,
        new SampleTaker<>(new DataReaderEntityStatisticsSeq()),
        (DataReaderWatcherListener<DataReaderEntityStatistics>) (sample, info) ->
            dataReaderEntityStatisticsMetricProcessor.process(sample, info)
    );

    dataReaderWatcherDataReaderEntityMatchedPublicationStatistics = new DataReaderWatcher<>(
        domainParticipant.lookup_datareader_by_name("Subscriber::DataReaderEntityMatchedPublicationStatistics"),
        readConditionParams,
        new SampleTaker<>(new DataReaderEntityMatchedPublicationStatisticsSeq()),
        (DataReaderWatcherListener<DataReaderEntityMatchedPublicationStatistics>) (sample, info) ->
            dataReaderEntityMatchedPublicationStatisticsMetricProcessor.process(sample, info)
    );

    dataReaderWatcherDataWriterDescription = new DataReaderWatcher<>(
        domainParticipant.lookup_datareader_by_name("Subscriber::DataWriterDescription"),
        readConditionParams,
        new SampleTaker<>(new DataWriterDescriptionSeq()),
        (DataReaderWatcherListener<DataWriterDescription>) (sample, info) ->
            dataWriterDescriptionMetricProcessor.process(sample, info)
    );

    dataReaderWatcherDataWriterEntityStatistics = new DataReaderWatcher<>(
        domainParticipant.lookup_datareader_by_name("Subscriber::DataWriterEntityStatistics"),
        readConditionParams,
        new SampleTaker<>(new DataWriterEntityStatisticsSeq()),
        (DataReaderWatcherListener<DataWriterEntityStatistics>) (sample, info) ->
            dataWriterEntityStatisticsMetricProcessor.process(sample, info)
    );

    dataReaderWatcherDataWriterEntityMatchedSubscriptionStatistics = new DataReaderWatcher<>(
        domainParticipant.lookup_datareader_by_name("Subscriber::DataWriterEntityMatchedSubscriptionStatistics"),
        readConditionParams,
        new SampleTaker<>(new DataWriterEntityMatchedSubscriptionStatisticsSeq()),
        (DataReaderWatcherListener<DataWriterEntityMatchedSubscriptionStatistics>) (sample, info) ->
            dataWriterEntityMatchedSubscriptionStatisticsMetricProcessor.process(sample, info)
    );

    dataReaderWatcherDataWriterEntityMatchedSubscriptionWithLocatorStatistics = new DataReaderWatcher<>(
        domainParticipant
            .lookup_datareader_by_name("Subscriber::DataWriterEntityMatchedSubscriptionWithLocatorStatistics"),
        readConditionParams,
        new SampleTaker<>(new DataWriterEntityMatchedSubscriptionWithLocatorStatisticsSeq()),
        (DataReaderWatcherListener<DataWriterEntityMatchedSubscriptionWithLocatorStatistics>) (sample, info) ->
            dataWriterEntityMatchedSubscriptionWithLocatorStatisticsMetricProcessor.process(sample, info)
    );
  }

  private void stopSubscription() {
    // signal termination
    dataReaderWatcherDomainParticipantDescription.close();
    dataReaderWatcherDomainParticipantEntityStatistics.close();
    dataReaderWatcherTopicDescription.close();
    dataReaderWatcherTopicEntityStatistics.close();
    dataReaderWatcherPublisherDescription.close();
    dataReaderWatcherSubscriberDescription.close();
    dataReaderWatcherDataReaderDescription.close();
    dataReaderWatcherDataReaderEntityStatistics.close();
    dataReaderWatcherDataReaderEntityMatchedPublicationStatistics.close();
    dataReaderWatcherDataWriterDescription.close();
    dataReaderWatcherDataWriterEntityStatistics.close();
    dataReaderWatcherDataWriterEntityMatchedSubscriptionStatistics.close();
    dataReaderWatcherDataWriterEntityMatchedSubscriptionWithLocatorStatistics.close();

    // set objects to null
    dataReaderWatcherDomainParticipantDescription = null;
    dataReaderWatcherDomainParticipantEntityStatistics = null;
    dataReaderWatcherTopicDescription = null;
    dataReaderWatcherTopicEntityStatistics = null;
    dataReaderWatcherPublisherDescription = null;
    dataReaderWatcherSubscriberDescription = null;
    dataReaderWatcherDataReaderDescription = null;
    dataReaderWatcherDataReaderEntityStatistics = null;
    dataReaderWatcherDataReaderEntityMatchedPublicationStatistics = null;
    dataReaderWatcherDataWriterDescription = null;
    dataReaderWatcherDataWriterEntityStatistics = null;
    dataReaderWatcherDataWriterEntityMatchedSubscriptionStatistics = null;
    dataReaderWatcherDataWriterEntityMatchedSubscriptionWithLocatorStatistics = null;

    // terminate http server
    httpServer.stop();
    httpServer = null;
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
