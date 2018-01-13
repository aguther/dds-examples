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

package com.github.aguther.dds.discovery.observer;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.InstanceHandleSeq;
import com.rti.dds.infrastructure.RETCODE_NOT_ENABLED;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.Subscriber;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Executors.class, BuiltinTopicObserver.class})
public class BuiltinTopicObserverTest {

  private static final String BUILTIN_TOPIC_NAME = "BuiltinTopic";

  private DataReader dataReader;
  private ExecutorService executorService;
  private BuiltinTopicObserver builtinTopicObserver;

  @Before
  public void setUp() {
    DomainParticipant domainParticipant = mock(DomainParticipant.class);
    Subscriber subscriber = mock(Subscriber.class);
    dataReader = mock(DataReader.class);

    doThrow(new RETCODE_NOT_ENABLED()).when(domainParticipant)
        .get_discovered_participants(new InstanceHandleSeq());
    when(domainParticipant.get_builtin_subscriber()).thenReturn(subscriber);
    when(subscriber.lookup_datareader(BUILTIN_TOPIC_NAME))
        .thenReturn(dataReader);

    mockStatic(Executors.class);
    executorService = mock(ExecutorService.class);
    when(Executors.newSingleThreadExecutor()).thenReturn(executorService);

    builtinTopicObserver = new BuiltinTopicObserver(
        domainParticipant,
        BUILTIN_TOPIC_NAME
    );
  }

  @After
  public void tearDown() {
    builtinTopicObserver.close();
  }

  @Test
  public void testOnDataAvailable() {
    // call on_data_available
    builtinTopicObserver.on_data_available(dataReader);
    // verify that executor was triggered
    verify(executorService, times(1)).submit(builtinTopicObserver);
  }

  @Test
  public void testRun() {
    builtinTopicObserver.run();
  }
}
