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

package com.github.aguther.dds.examples.discovery;

import com.github.aguther.dds.examples.discovery.TopicRoute.Direction;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.builtin.ParticipantBuiltinTopicData;
import com.rti.dds.infrastructure.InstanceHandleSeq;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.ServiceQosPolicyKind;
import com.rti.dds.infrastructure.StringSeq;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import com.rti.dds.topic.BuiltinTopicKey_t;
import idl.RTI.RoutingService.Administration.CommandKind;
import idl.RTI.RoutingService.Administration.CommandRequest;
import idl.RTI.RoutingService.Administration.CommandResponse;
import idl.RTI.RoutingService.Administration.CommandResponseKind;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteObserver implements PublicationObserverListener, SubscriptionObserverListener {

  private static final Logger log;
  private static final String TARGET_ROUTER;

  static {
    log = LoggerFactory.getLogger(RouteObserver.class);
    TARGET_ROUTER = "dds-examples-routing";
  }

  private final Object mappingLock;
  private final HashMap<Session, Multimap<TopicRoute, InstanceHandle_t>> mapping;

  private final DomainParticipant domainParticipant;
  private final RoutingServiceCommander routingServiceCommander;

  public RouteObserver(
      DomainParticipant domainParticipant,
      RoutingServiceCommander routingServiceCommander
  ) {
    mappingLock = new Object();
    mapping = new HashMap<>();

    this.domainParticipant = domainParticipant;
    this.routingServiceCommander = routingServiceCommander;
  }

  @Override
  public void publicationDiscovered(
      InstanceHandle_t instanceHandle,
      PublicationBuiltinTopicData data
  ) {
    if (shouldIgnore(data.topic_name, data.participant_key)) {
      return;
    }

    handleDiscovered(
        instanceHandle,
        Direction.OUT,
        data.topic_name,
        data.partition.name
    );
  }

  @Override
  public void publicationLost(
      InstanceHandle_t instanceHandle,
      PublicationBuiltinTopicData data
  ) {
    if (shouldIgnore(data.topic_name, data.participant_key)) {
      return;
    }

    handleLost(
        instanceHandle,
        Direction.OUT,
        data.topic_name,
        data.partition.name
    );
  }

  @Override
  public void subscriptionDiscovered(
      InstanceHandle_t instanceHandle,
      SubscriptionBuiltinTopicData data
  ) {
    if (shouldIgnore(data.topic_name, data.participant_key)) {
      return;
    }

    handleDiscovered(
        instanceHandle,
        Direction.IN,
        data.topic_name,
        data.partition.name
    );
  }

  @Override
  public void subscriptionLost(
      InstanceHandle_t instanceHandle,
      SubscriptionBuiltinTopicData data
  ) {
    if (shouldIgnore(data.topic_name, data.participant_key)) {
      return;
    }

    handleLost(
        instanceHandle,
        Direction.IN,
        data.topic_name,
        data.partition.name
    );
  }

  private boolean shouldIgnore(
      String topicName,
      BuiltinTopicKey_t participantKey
  ) {
    // ignore all rti internal topics
    if (topicName.startsWith("rti")) {
      return true;
    }

    // get data of parent domain participant
    ParticipantBuiltinTopicData participantData = getParticipantBuiltinTopicData(participantKey);

    // check if participant belongs to a routing service
    if (participantData != null) {
      return (participantData.service.kind == ServiceQosPolicyKind.ROUTING_SERVICE_QOS);
    }

    // do not ignore
    return false;
  }

  private ParticipantBuiltinTopicData getParticipantBuiltinTopicData(
      BuiltinTopicKey_t participantKey
  ) {
    // get discovered participants
    InstanceHandleSeq participantHandles = new InstanceHandleSeq();
    domainParticipant.get_discovered_participants(participantHandles);

    // iterate over handles
    ParticipantBuiltinTopicData participantData = new ParticipantBuiltinTopicData();
    for (Object participantHandle : participantHandles) {
      domainParticipant.get_discovered_participant_data(
          participantData,
          (InstanceHandle_t) participantHandle
      );

      if (participantData.key.equals(participantKey)) {
        return participantData;
      }
    }

    // nothing found
    return null;
  }

  private void handleDiscovered(
      InstanceHandle_t instanceHandle,
      Direction direction,
      String topicName,
      StringSeq partitions
  ) {
    synchronized (mappingLock) {
      // create routes for all partitions we discovered
      if (partitions.isEmpty()) {
        // create session
        Session session = new Session(topicName, "");
        // create topic session if first item discovered
        if (!mapping.containsKey(session)) {
          mapping.put(session, ArrayListMultimap.create());
          log.info(
              "Create session: topic='{}', partition='{}'",
              session.getTopic(),
              session.getPartition()
          );
          createSession(session);
        }

        // create topic route object
        TopicRoute topicRoute = new TopicRoute(direction, topicName);
        // check if topic route is about to be created
        if (!mapping.get(session).containsKey(topicRoute)) {
          log.info(
              "Create route: topic='{}', partition='{}', direction='{}'",
              session.getTopic(),
              session.getPartition(),
              topicRoute.getDirection()
          );
          createTopicRoute(direction, topicName, "");
        }
        // add instance handle to topic route
        mapping.get(session).put(topicRoute, instanceHandle);

      } else {
        for (Object partition : partitions) {
          // create session
          Session session = new Session(topicName, partition.toString());
          // create topic session if first item discovered
          if (!mapping.containsKey(session)) {
            mapping.put(session, ArrayListMultimap.create());
            log.info(
                "Create session: topic='{}', partition='{}'",
                session.getTopic(),
                session.getPartition()
            );
            createSession(session);
          }

          // create topic route object
          TopicRoute topicRoute = new TopicRoute(direction, topicName);
          // check if topic route is about to be created
          if (!mapping.get(session).containsKey(topicRoute)) {
            log.info(
                "Create route: topic='{}', partition='{}', direction='{}'",
                session.getTopic(),
                session.getPartition(),
                topicRoute.getDirection()
            );
            createTopicRoute(direction, topicName, partition.toString());
          }
          // add instance handle to topic route
          mapping.get(session).put(topicRoute, instanceHandle);
        }
      }
    }
  }

  private void handleLost(
      InstanceHandle_t instanceHandle,
      Direction direction,
      String topicName,
      StringSeq partitions
  ) {
    synchronized (mappingLock) {
      // delete routes for all partitions we lost
      if (partitions.isEmpty()) {
        // create session
        Session session = new Session(topicName, "");
        // create topic route object
        TopicRoute topicRoute = new TopicRoute(direction, topicName);

        // remove instance handle from topic route
        mapping.get(session).remove(topicRoute, instanceHandle);

        // check if route was deleted
        if (!mapping.get(session).containsKey(topicRoute)) {
          log.info(
              "Delete route: topic='{}', partition='{}', direction='{}'",
              session.getTopic(),
              session.getPartition(),
              topicRoute.getDirection()
          );
          deleteTopicRoute(direction, topicName, "");
        }

        // delete topic session if last items was removed
        if (mapping.get(session).isEmpty()) {
          mapping.remove(session);
          log.info(
              "Delete session: topic='{}', partition='{}'",
              session.getTopic(),
              session.getPartition()
          );
          deleteSession(session);
        }
      } else {
        for (Object partition : partitions) {
          // create session
          Session session = new Session(topicName, partition.toString());
          // create topic route object
          TopicRoute topicRoute = new TopicRoute(direction, topicName);

          // remove instance handle from topic route
          mapping.get(session).remove(topicRoute, instanceHandle);

          // check if route is deleted
          if (!mapping.get(session).containsKey(topicRoute)) {
            log.info(
                "Delete route: topic='{}', partition='{}', direction='{}'",
                session.getTopic(),
                session.getPartition(),
                topicRoute.getDirection()
            );
            deleteTopicRoute(direction, topicName, partition.toString());
          }

          // delete topic session if last items was removed
          if (mapping.get(session).isEmpty()) {
            mapping.remove(session);
            log.info(
                "Delete session: topic='{}', partition='{}'",
                session.getTopic(),
                session.getPartition()
            );
            deleteSession(session);
          }
        }
      }
    }
  }

  private void createSession(
      Session session
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommander.createCommandRequest();
    commandRequest.target_router = TARGET_ROUTER;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    commandRequest.command.entity_desc.name = "Default";
    commandRequest.command.entity_desc.xml_url.content = String
        .format(
            "str://\"<session name=\"%1$s\"><publisher_qos><partition><name><element>%2$s</element></name></partition></publisher_qos><subscriber_qos><partition><name><element>%2$s</element></name></partition></subscriber_qos></session>\"",
            String.format("%s(%s)", session.getTopic(), session.getPartition()),
            session.getPartition());
    commandRequest.command.entity_desc.xml_url.is_final = true;

    // send request
    CommandResponse reply = routingServiceCommander.sendRequest(commandRequest);

    // reply received?
    if (reply == null) {
      log.error(
          "No reply received when creating session for topic '{};{}'",
          session.getTopic(),
          session.getPartition()
      );
      return;
    }
    // reply success or failed?
    if (reply.kind == CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK) {
      log.info(
          "Created session for topic='{}', partition='{}'",
          session.getTopic(),
          session.getPartition()
      );
    } else {
      log.error(
          "Failed to create session for topic='{}', partition='{}', reason: {}, {}",
          session.getTopic(),
          session.getPartition(),
          reply.kind,
          reply.message
      );
    }
  }

  private void deleteSession(
      Session session
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommander.createCommandRequest();
    commandRequest.target_router = TARGET_ROUTER;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    commandRequest.command.entity_name = String.format(
        "Default::%s", String.format("%s(%s)", session.getTopic(), session.getPartition()));

    // send request
    CommandResponse reply = routingServiceCommander.sendRequest(commandRequest);

    // reply received?
    if (reply == null) {
      log.error(
          "No reply received when deleting session for topic='{}', partition='{}'",
          session.getTopic(),
          session.getPartition()
      );
      return;
    }
    // reply success or failed?
    if (reply.kind == CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK) {
      log.info(
          "Deleted session for topic='{}', partition='{}'",
          session.getTopic(),
          session.getPartition()
      );
    } else {
      log.error(
          "Failed to delete session for topic='{}', partition='{}', reason: {}, {}",
          session.getTopic(),
          session.getPartition(),
          reply.kind,
          reply.message
      );
    }
  }

  private void createTopicRoute(
      Direction direction,
      String topicName,
      String partition
  ) {
    // detect input participant
    int inputParticipant = direction == Direction.OUT ? 1 : 2;

    // create request
    CommandRequest commandRequest = routingServiceCommander.createCommandRequest();
    commandRequest.target_router = TARGET_ROUTER;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_CREATE;
    commandRequest.command.entity_desc.name = String.format("Default::%s(%s)", topicName, partition);
    commandRequest.command.entity_desc.xml_url.content = String.format(
        "str://\"<auto_topic_route name=\"%1$s\"><input participant=\"%2$d\"><allow_topic_name_filter>%3$s</allow_topic_name_filter><datareader_qos base_name=\"QosLibrary::Base\"/></input><output><allow_topic_name_filter>%3$s</allow_topic_name_filter><datawriter_qos base_name=\"QosLibrary::Base\"/></output></auto_topic_route>\"",
        direction.toString(),
        inputParticipant,
        topicName);
    commandRequest.command.entity_desc.xml_url.is_final = true;

    // send request
    CommandResponse reply = routingServiceCommander.sendRequest(commandRequest);

    // reply received?
    if (reply == null) {
      log.error(
          "No reply received when creating route for topic='{}', partition='{}', direction='{}'",
          topicName,
          partition,
          direction.toString()
      );
      return;
    }
    // reply success or failed?
    if (reply.kind == CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK) {
      log.info(
          "Created route for topic='{}', partition='{}', direction='{}'",
          topicName,
          partition,
          direction.toString()
      );
    } else {
      log.error(
          "Failed to create route for topic='{}', partition='{}', direction='{}', reason: {}, {}",
          topicName,
          partition,
          direction.toString(),
          reply.kind,
          reply.message
      );
    }
  }

  private void deleteTopicRoute(
      Direction direction,
      String topicName,
      String partition
  ) {
    // create request
    CommandRequest commandRequest = routingServiceCommander.createCommandRequest();
    commandRequest.target_router = TARGET_ROUTER;
    commandRequest.command._d = CommandKind.RTI_ROUTING_SERVICE_COMMAND_DELETE;
    commandRequest.command.entity_name = String.format(
        "Default::%1$s(%2$s)::%3$s", topicName, partition, direction.toString());

    // send request
    CommandResponse reply = routingServiceCommander.sendRequest(commandRequest);

    // reply received?
    if (reply == null) {
      log.error(
          "No reply received when deleting route for topic='{}', partition='{}', direction='{}'",
          topicName,
          partition,
          direction.toString()
      );
      return;
    }
    // reply success or failed?
    if (reply.kind == CommandResponseKind.RTI_ROUTING_SERVICE_COMMAND_RESPONSE_OK) {
      log.info(
          "Deleted route for topic='{}', partition='{}', direction='{}'",
          topicName,
          partition,
          direction.toString()
      );
    } else {
      log.error(
          "Failed to delete route for topic='{}', partition='{}', direction='{}', reason: {}, {}",
          topicName,
          partition,
          direction.toString(),
          reply.kind,
          reply.message
      );
    }
  }
}
