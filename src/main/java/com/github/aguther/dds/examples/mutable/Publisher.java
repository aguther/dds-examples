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

package com.github.aguther.dds.examples.mutable;

import com.github.aguther.dds.util.Slf4jDdsLogger;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import idl.v1.MutableTypeTypeSupport;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Publisher {

  private static final Logger log;

  private static boolean shouldTerminate;

  private static DomainParticipant domainParticipant;

  private static Thread publishThread;
  private static MutableTypePublisher mutableTypePublisher;

  static {
    log = LoggerFactory.getLogger(Publisher.class);
  }

  public static void main(
      String[] args
  ) {
    // register shutdown hook
    registerShutdownHook();

    // startup DDS
    startupDds();

    // start publishing
    startPublish();

    // wait for signal to terminate
    waitForTerminateSignal();

    // stop publish
    stopPublish();

    // shutdown DDS
    shutdownDds();
  }

  private static void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Shutdown signal received...");
      shouldTerminate = true;
    }));
  }

  private static void startupDds() {
    // register logger DDS messages
    try {
      Slf4jDdsLogger.createRegisterLogger();
    } catch (IOException e) {
      log.error("Failed to create and register DDS logging device.", e);
      return;
    }

    // register all types needed (this must be done before creation of the domain participant)
    DomainParticipantFactory.get_instance().register_type_support(
        MutableTypeTypeSupport.get_instance(),
        MutableTypeTypeSupport.get_type_name()
    );

    // create participant from config
    domainParticipant = DomainParticipantFactory.get_instance().create_participant_from_config(
        "DomainParticipantLibrary::MutablePublisher"
    );
  }

  private static void waitForTerminateSignal() {
    while (true) {
      if (shouldTerminate) {
        break;
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private static void startPublish() {
    // create shape publisher
    mutableTypePublisher = new MutableTypePublisher(
        domainParticipant,
        "Publisher::MutableTypeDataWriter",
        1000
    );

    // create and start thread
    publishThread = new Thread(mutableTypePublisher);
    publishThread.start();
  }

  private static void stopPublish() {
    // check if we need to stop publish
    if (mutableTypePublisher == null) {
      return;
    }

    // signal termination
    mutableTypePublisher.stop();

    // wait for thread to finish
    try {
      publishThread.join();
    } catch (InterruptedException e) {
      log.error("Interrupted on join of publisher thread.", e);
      Thread.currentThread().interrupt();
    }

    // set objects to null
    publishThread = null;
    mutableTypePublisher = null;
  }

  private static void shutdownDds() {
    // delete domain participant
    DomainParticipantFactory.get_instance().delete_participant(domainParticipant);
    domainParticipant = null;
  }
}
