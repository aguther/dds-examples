/*
 * MIT License
 *
 * Copyright (c) 2018 Andreas Guther
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

package com.github.aguther.dds.examples.kafka;

import com.github.aguther.dds.util.KafkaCdrTypeDeserializer;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import idl.ShapeTypeExtended;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Subscriber extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(Subscriber.class);

  private static final String TOPIC = "example";
  private static final String BOOTSTRAP_SERVERS = "localhost:9092";

  private static Subscriber serviceInstance;

  private Consumer<Long, ShapeTypeExtended> consumer;

  public static void main(
      final String[] args
  ) {
    // register shutdown hook
    registerShutdownHook();

    // create service
    serviceInstance = new Subscriber();

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
        String.format("ShutdownHook-%s", Subscriber.class.getName())
    ));
  }

  @Override
  protected void startUp() {
    // log service start
    LOGGER.info("Service is starting");

    // configure properties for consumer
    Properties config = new Properties();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, Subscriber.class.getName());
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaCdrTypeDeserializer.class.getName());
    config.put(KafkaCdrTypeDeserializer.VALUE_DESERIALIZER_CLASS_CONFIG_TYPE,
        "idl.ShapeTypeExtended");
    config.put(KafkaCdrTypeDeserializer.VALUE_DESERIALIZER_CLASS_CONFIG_TYPE_SUPPORT,
        "idl.ShapeTypeExtendedTypeSupport");

    // create consumer
    consumer = new KafkaConsumer<>(config);

    // subscribe to topic
    consumer.subscribe(Collections.singletonList(TOPIC));

    // log service start
    LOGGER.info("Service start finished");
  }

  @Override
  protected void run() {
    while (serviceInstance.state() == State.RUNNING) {
      // get records
      final ConsumerRecords<Long, ShapeTypeExtended> consumerRecords = consumer.poll(1000);

      // check if any records received
      if (consumerRecords.count() == 0) {
        continue;
      }

      // log all received records
      consumerRecords.forEach(record ->
          LOGGER.info(
              "Received record key='{}', value='{}', partition='{}', offset='{}'",
              record.key(),
              record.value(),
              record.partition(),
              record.offset()
          )
      );

      // commit received records
      consumer.commitAsync();
    }
  }

  @Override
  protected void shutDown() {
    // log service start
    LOGGER.info("Service is shutting down");

    if (consumer != null) {
      // unsubscribe from topics
      consumer.unsubscribe();

      // close consumer
      consumer.close();
      consumer = null;
    }

    // log service start
    LOGGER.info("Service shutdown finished");
  }
}
