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

package io.github.aguther.dds.util;

import static org.junit.Assert.assertEquals;

import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.RETCODE_BAD_PARAMETER;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DurationFactoryTest {

  @Test
  public void testFrom() {
    checkDuration(1, TimeUnit.DAYS, 86400, 0);

    checkDuration(6, TimeUnit.MINUTES, 360, 0);

    checkDuration(1, TimeUnit.SECONDS, 1, 0);
    checkDuration(10, TimeUnit.SECONDS, 10, 0);
    checkDuration(Integer.MAX_VALUE, TimeUnit.SECONDS, Integer.MAX_VALUE, 0);

    checkDuration(1500, TimeUnit.MILLISECONDS, 1, 500000000);
    checkDuration(10500, TimeUnit.MILLISECONDS, 10, 500000000);
    checkDuration(Integer.MAX_VALUE, TimeUnit.MILLISECONDS, 2147483, 647000000);
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testFromBelowZero() {
    thrown.expect(RETCODE_BAD_PARAMETER.class);
    Duration_t duration = DurationFactory.from(Integer.MIN_VALUE, TimeUnit.MILLISECONDS);
  }

  private void checkDuration(
      long time,
      TimeUnit unit,
      int expectedSec,
      int expectedNanoSeconds
  ) {
    Duration_t duration = DurationFactory.from(time, unit);

    assertEquals(duration.sec, expectedSec);
    assertEquals(duration.nanosec, expectedNanoSeconds);
  }

  @Test
  public void testTo() {
    assertEquals(1500, DurationFactory.to(TimeUnit.MILLISECONDS, Duration_t.from_millis(1500)));
    assertEquals(10500, DurationFactory.to(TimeUnit.MILLISECONDS, Duration_t.from_millis(10500)));
    assertEquals(1, DurationFactory.to(TimeUnit.DAYS, Duration_t.from_seconds(86400)));
  }
}
