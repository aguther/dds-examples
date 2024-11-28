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

package io.github.aguther.dds.support.publication;

import static com.google.common.base.Preconditions.checkNotNull;

import com.rti.dds.domain.builtin.ParticipantBuiltinTopicData;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.InstanceHandleSeq;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.LocatorSeq;
import com.rti.dds.infrastructure.Locator_t;
import com.rti.dds.infrastructure.SampleIdentity_t;
import com.rti.dds.infrastructure.StatusCondition;
import com.rti.dds.infrastructure.Time_t;
import com.rti.dds.infrastructure.WriteParams_t;
import com.rti.dds.publication.DataWriter;
import com.rti.dds.publication.DataWriterCacheStatus;
import com.rti.dds.publication.DataWriterListener;
import com.rti.dds.publication.DataWriterProtocolStatus;
import com.rti.dds.publication.DataWriterQos;
import com.rti.dds.publication.LivelinessLostStatus;
import com.rti.dds.publication.OfferedDeadlineMissedStatus;
import com.rti.dds.publication.OfferedIncompatibleQosStatus;
import com.rti.dds.publication.PublicationMatchedStatus;
import com.rti.dds.publication.Publisher;
import com.rti.dds.publication.ReliableReaderActivityChangedStatus;
import com.rti.dds.publication.ReliableWriterCacheChangedStatus;
import com.rti.dds.publication.ServiceRequestAcceptedStatus;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import com.rti.dds.topic.Topic;

public class DataWriterDecorator implements DataWriter {

  private DataWriter dataWriter;

  public DataWriterDecorator(
    DataWriter dataWriter
  ) {
    checkNotNull(dataWriter);
    this.dataWriter = dataWriter;
  }

  @Override
  public void set_qos(
    DataWriterQos dataWriterQos
  ) {
    dataWriter.set_qos(dataWriterQos);
  }

  @Override
  public void set_qos_with_profile(
    String s,
    String s1
  ) {
    dataWriter.set_qos_with_profile(s, s1);
  }

  @Override
  public void get_qos(
    DataWriterQos dataWriterQos
  ) {
    dataWriter.get_qos(dataWriterQos);
  }

  @Override
  public void set_listener(
    DataWriterListener dataWriterListener,
    int i
  ) {
    dataWriter.set_listener(dataWriterListener, i);
  }

  @Override
  public DataWriterListener get_listener() {
    return dataWriter.get_listener();
  }

  @Override
  public void call_listenerT(
    int i
  ) {
    dataWriter.call_listenerT(i);
  }

  @Override
  public void get_liveliness_lost_status(
    LivelinessLostStatus livelinessLostStatus
  ) {
    dataWriter.get_liveliness_lost_status(livelinessLostStatus);
  }

  @Override
  public void get_offered_deadline_missed_status(
    OfferedDeadlineMissedStatus offeredDeadlineMissedStatus
  ) {
    dataWriter.get_offered_deadline_missed_status(offeredDeadlineMissedStatus);
  }

  @Override
  public void get_offered_incompatible_qos_status(
    OfferedIncompatibleQosStatus offeredIncompatibleQosStatus
  ) {
    dataWriter.get_offered_incompatible_qos_status(offeredIncompatibleQosStatus);
  }

  @Override
  public void get_publication_matched_status(
    PublicationMatchedStatus publicationMatchedStatus
  ) {
    dataWriter.get_publication_matched_status(publicationMatchedStatus);
  }

  @Override
  public void get_reliable_writer_cache_changed_status(
    ReliableWriterCacheChangedStatus reliableWriterCacheChangedStatus
  ) {
    dataWriter.get_reliable_writer_cache_changed_status(reliableWriterCacheChangedStatus);
  }

  @Override
  public void get_reliable_reader_activity_changed_status(
    ReliableReaderActivityChangedStatus reliableReaderActivityChangedStatus
  ) {
    dataWriter.get_reliable_reader_activity_changed_status(reliableReaderActivityChangedStatus);
  }

  @Override
  public void get_datawriter_cache_status(
    DataWriterCacheStatus dataWriterCacheStatus
  ) {
    dataWriter.get_datawriter_cache_status(dataWriterCacheStatus);
  }

  @Override
  public void get_datawriter_protocol_status(
    DataWriterProtocolStatus dataWriterProtocolStatus
  ) {
    dataWriter.get_datawriter_protocol_status(dataWriterProtocolStatus);
  }

  @Override
  public void get_matched_subscription_datawriter_protocol_status(
    DataWriterProtocolStatus dataWriterProtocolStatus,
    InstanceHandle_t instanceHandle
  ) {
    dataWriter.get_matched_subscription_datawriter_protocol_status(
      dataWriterProtocolStatus,
      instanceHandle
    );
  }

  @Override
  public void get_matched_subscription_datawriter_protocol_status_by_locator(
    DataWriterProtocolStatus dataWriterProtocolStatus,
    Locator_t locator
  ) {
    dataWriter.get_matched_subscription_datawriter_protocol_status_by_locator(
      dataWriterProtocolStatus,
      locator
    );
  }

  @Override
  public void get_service_request_accepted_status(
    ServiceRequestAcceptedStatus serviceRequestAcceptedStatus
  ) {
    dataWriter.get_service_request_accepted_status(serviceRequestAcceptedStatus);
  }

  @Override
  public void set_liveliness_lost_statusT(
    LivelinessLostStatus livelinessLostStatus
  ) {
    dataWriter.set_liveliness_lost_statusT(livelinessLostStatus);
  }

  @Override
  public void set_offered_deadline_missed_statusT(
    OfferedDeadlineMissedStatus offeredDeadlineMissedStatus
  ) {
    dataWriter.set_offered_deadline_missed_statusT(offeredDeadlineMissedStatus);
  }

  @Override
  public void set_offered_incompatible_qos_statusT(
    OfferedIncompatibleQosStatus offeredIncompatibleQosStatus
  ) {
    dataWriter.set_offered_incompatible_qos_statusT(offeredIncompatibleQosStatus);
  }

  @Override
  public void set_publication_matched_statusT(
    PublicationMatchedStatus publicationMatchedStatus
  ) {
    dataWriter.set_publication_matched_statusT(publicationMatchedStatus);
  }

  @Override
  public void set_reliable_writer_cache_changed_statusT(
    ReliableWriterCacheChangedStatus reliableWriterCacheChangedStatus
  ) {
    dataWriter.set_reliable_writer_cache_changed_statusT(reliableWriterCacheChangedStatus);
  }

  @Override
  public void set_reliable_reader_activity_changed_statusT(
    ReliableReaderActivityChangedStatus reliableReaderActivityChangedStatus
  ) {
    dataWriter.set_reliable_reader_activity_changed_statusT(reliableReaderActivityChangedStatus);
  }

  @Override
  public void set_service_request_accepted_statusT(
    ServiceRequestAcceptedStatus serviceRequestAcceptedStatus
  ) {
    dataWriter.set_service_request_accepted_statusT(serviceRequestAcceptedStatus);
  }

  @Override
  public void get_matched_subscription_locators(
    LocatorSeq locatorSeq
  ) {
    dataWriter.get_matched_subscription_locators(locatorSeq);
  }

  @Override
  public void get_matched_subscriptions(
    InstanceHandleSeq instanceHandleSeq
  ) {
    dataWriter.get_matched_subscriptions(instanceHandleSeq);
  }

  @Override
  public boolean is_matched_subscription_active(InstanceHandle_t instanceHandle_t) {
    return dataWriter.is_matched_subscription_active(instanceHandle_t);
  }

  @Override
  public void get_matched_subscription_data(
    SubscriptionBuiltinTopicData subscriptionBuiltinTopicData,
    InstanceHandle_t instanceHandle
  ) {
    dataWriter.get_matched_subscription_data(
      subscriptionBuiltinTopicData,
      instanceHandle
    );
  }

  @Override
  public void get_matched_subscription_participant_data(
    ParticipantBuiltinTopicData participantBuiltinTopicData,
    InstanceHandle_t instanceHandle
  ) {
    dataWriter.get_matched_subscription_participant_data(
      participantBuiltinTopicData,
      instanceHandle
    );
  }

  @Override
  public Topic get_topic() {
    return dataWriter.get_topic();
  }

  @Override
  public Publisher get_publisher() {
    return dataWriter.get_publisher();
  }

  @Override
  public void wait_for_acknowledgments(
    Duration_t duration
  ) {
    dataWriter.wait_for_acknowledgments(duration);
  }

  @Override
  public void wait_for_sample_acknowledgment(
    SampleIdentity_t sampleIdentity,
    Duration_t duration
  ) {
    dataWriter.wait_for_sample_acknowledgment(sampleIdentity, duration);
  }

  @Override
  public boolean is_sample_app_acknowledged(
    SampleIdentity_t sampleIdentity
  ) {
    return dataWriter.is_sample_app_acknowledged(sampleIdentity);
  }

  @Override
  public void wait_for_asynchronous_publishing(
    Duration_t duration
  ) {
    dataWriter.wait_for_asynchronous_publishing(duration);
  }

  @Override
  public void assert_liveliness() {
    dataWriter.assert_liveliness();
  }

  @Override
  public void flush() {
    dataWriter.flush();
  }

  @Override
  public InstanceHandle_t register_instance_untyped(
    Object o
  ) {
    return dataWriter.register_instance_untyped(o);
  }

  @Override
  public InstanceHandle_t register_instance_w_timestamp_untyped(
    Object o,
    Time_t time
  ) {
    return dataWriter.register_instance_w_timestamp_untyped(o, time);
  }

  @Override
  public InstanceHandle_t register_instance_w_params_untyped(
    Object o,
    WriteParams_t writeParams
  ) {
    return dataWriter.register_instance_w_params_untyped(o, writeParams);
  }

  @Override
  public void unregister_instance_untyped(
    Object o,
    InstanceHandle_t instanceHandle
  ) {
    dataWriter.unregister_instance_untyped(o, instanceHandle);
  }

  @Override
  public void unregister_instance_w_timestamp_untyped(
    Object o,
    InstanceHandle_t instanceHandle,
    Time_t time
  ) {
    dataWriter.unregister_instance_w_timestamp_untyped(o, instanceHandle, time);
  }

  @Override
  public void unregister_instance_w_params_untyped(
    Object o,
    WriteParams_t writeParams
  ) {
    dataWriter.register_instance_w_params_untyped(o, writeParams);
  }

  @Override
  public void write_untyped(
    Object o,
    InstanceHandle_t instanceHandle
  ) {
    dataWriter.write_untyped(o, instanceHandle);
  }

  @Override
  public void write_w_timestamp_untyped(
    Object o,
    InstanceHandle_t instanceHandle,
    Time_t time
  ) {
    dataWriter.write_w_timestamp_untyped(o, instanceHandle, time);
  }

  @Override
  public void write_w_params_untyped(
    Object o,
    WriteParams_t writeParams
  ) {
    dataWriter.write_w_params_untyped(o, writeParams);
  }

  @Override
  public void dispose_untyped(
    Object o,
    InstanceHandle_t instanceHandle
  ) {
    dataWriter.dispose_untyped(o, instanceHandle);
  }

  @Override
  public void dispose_w_timestamp_untyped(
    Object o,
    InstanceHandle_t instanceHandle,
    Time_t time
  ) {
    dataWriter.dispose_w_timestamp_untyped(o, instanceHandle, time);
  }

  @Override
  public void dispose_w_params_untyped(
    Object o,
    WriteParams_t writeParams
  ) {
    dataWriter.dispose_w_params_untyped(o, writeParams);
  }

  @Override
  public void get_key_value_untyped(
    Object o,
    InstanceHandle_t instanceHandle
  ) {
    dataWriter.get_key_value_untyped(o, instanceHandle);
  }

  @Override
  public InstanceHandle_t lookup_instance_untyped(
    Object o
  ) {
    return dataWriter.lookup_instance_untyped(o);
  }

  @Override
  public void take_discovery_snapshot() {
    dataWriter.take_discovery_snapshot();
  }

  @Override
  public void take_discovery_snapshot(String s) {
    dataWriter.take_discovery_snapshot(s);
  }

  @Override
  public void enable() {
    dataWriter.enable();
  }

  @Override
  public StatusCondition get_statuscondition() {
    return dataWriter.get_statuscondition();
  }

  @Override
  public int get_status_changes() {
    return dataWriter.get_status_changes();
  }

  @Override
  public InstanceHandle_t get_instance_handle() {
    return dataWriter.get_instance_handle();
  }

  @Override
  public void lock() {
    dataWriter.lock();
  }

  @Override
  public void unlock() {
    dataWriter.unlock();
  }
}
