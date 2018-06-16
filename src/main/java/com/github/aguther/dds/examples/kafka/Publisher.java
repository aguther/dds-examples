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

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Publisher extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(Publisher.class);

  private static final String TOPIC = "example";
  private static final String BOOTSTRAP_SERVERS = "localhost:9092";

  private static Publisher serviceInstance;

  private Producer<Long, String> producer;

  public static void main(
      final String[] args
  ) {
    // register shutdown hook
    registerShutdownHook();

    // create service
    serviceInstance = new Publisher();

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
          }
          LOGGER.info("Shutdown signal finished");
        },
        String.format("ShutdownHook-%s", Publisher.class.getName())
    ));
  }

  @Override
  protected void startUp() {
    // log service start
    LOGGER.info("Service is starting");

    // configure properties for producer
    Properties config = new Properties();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    config.put(ProducerConfig.CLIENT_ID_CONFIG, Publisher.class.getName());
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    // create producer
    producer = new KafkaProducer<>(config);

    // log service start
    LOGGER.info("Service start finished");
  }

  @Override
  protected void run() throws Exception {
    long key = 0;

    while (serviceInstance.state() == State.RUNNING) {
      // create record
      final ProducerRecord<Long, String> record = new ProducerRecord<>(TOPIC, key, "Hello World!");

      // send record and get result
      RecordMetadata metadata = producer.send(record).get();

      // log information if requested
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info(
            "sent record(key={} value={}) meta(partition={}, offset={})",
            record.key(),
            record.value(),
            metadata.partition(),
            metadata.offset()
        );
      }

      // set new key
      key = (key + 1) % 10;

      // sleep some time
      Thread.sleep(250);
    }
  }

  @Override
  protected void shutDown() {
    // log service start
    LOGGER.info("Service is shutting down");

    // shutdown DDS
    if (producer != null) {
      producer.flush();
      producer.close();
      producer = null;
    }

    // log service start
    LOGGER.info("Service shutdown finished");
  }
}
