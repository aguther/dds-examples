package com.github.aguther.dds.util;

import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantFactoryQos;

public class AutoEnableCreatedEntitiesHelper {

  public static void enable() {
    switchValueTo(true);
  }

  public static void disable() {
    switchValueTo(false);
  }

  private static void switchValueTo(
      boolean value
  ) {
    // create new QoS object
    DomainParticipantFactoryQos domainParticipantFactoryQos = new DomainParticipantFactoryQos();

    // get current QoS from domain participant factory
    DomainParticipantFactory.get_instance().get_qos(domainParticipantFactoryQos);

    // update needed?
    if (domainParticipantFactoryQos.entity_factory.autoenable_created_entities != value) {
      // update value
      domainParticipantFactoryQos.entity_factory.autoenable_created_entities = value;

      // update QoS on domain participant factory
      DomainParticipantFactory.get_instance().set_qos(domainParticipantFactoryQos);
    }
  }
}
