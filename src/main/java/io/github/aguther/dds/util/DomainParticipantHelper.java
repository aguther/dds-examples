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

package io.github.aguther.dds.util;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.InstanceHandleSeq;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_NOT_ENABLED;

public class DomainParticipantHelper {

  private DomainParticipantHelper() {
  }

  /**
   * Checks if a provided domain participant is enabled or not.
   * <p>
   * IMPORTANT: this method triggers a DDS error message that can be safely ignored:
   * "DDS_DomainParticipant_get_discovered_participants:not enabled"
   *
   * @param domainParticipant DomainParticipant that should be checked
   * @return True if enabled, false if not enabled
   */
  public static boolean isEnabled(
    final DomainParticipant domainParticipant
  ) {
    try {
      // try to get discovered participants; this call will trigger
      // a specific exception if the domain participant is not enabled
      InstanceHandleSeq instanceHandleSeq = new InstanceHandleSeq();
      domainParticipant.get_discovered_participants(instanceHandleSeq);
    } catch (RETCODE_NOT_ENABLED notEnabled) {
      // domain participant is not enabled
      return false;
    } catch (RETCODE_ERROR error) {
      // we got an error, but it seems to be enabled
      return true;
    }
    // domain participant is enabled
    return true;
  }
}
