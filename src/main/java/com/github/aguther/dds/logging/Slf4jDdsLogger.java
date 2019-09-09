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

package com.github.aguther.dds.logging;


import static com.rti.ndds.config.LogLevel.NDDS_CONFIG_LOG_LEVEL_DEBUG;
import static com.rti.ndds.config.LogLevel.NDDS_CONFIG_LOG_LEVEL_ERROR;
import static com.rti.ndds.config.LogLevel.NDDS_CONFIG_LOG_LEVEL_STATUS_LOCAL;
import static com.rti.ndds.config.LogLevel.NDDS_CONFIG_LOG_LEVEL_STATUS_REMOTE;
import static com.rti.ndds.config.LogLevel.NDDS_CONFIG_LOG_LEVEL_WARNING;

import com.rti.ndds.config.LogMessage;
import com.rti.ndds.config.LogPrintFormat;
import com.rti.ndds.config.LogVerbosity;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jDdsLogger implements com.rti.ndds.config.LoggerDevice {

  private static final Logger LOGGER = LoggerFactory.getLogger(Slf4jDdsLogger.class);

  public static Slf4jDdsLogger createRegisterLogger() throws IOException {
    // create logger
    Slf4jDdsLogger slf4jDdsLogger = new Slf4jDdsLogger();

    // register logger
    com.rti.ndds.config.Logger.get_instance().set_output_device(slf4jDdsLogger);
    com.rti.ndds.config.Logger.get_instance().set_print_format(LogPrintFormat.NDDS_CONFIG_LOG_PRINT_FORMAT_TIMESTAMPED);

    // set log level
    if (LOGGER.isTraceEnabled()) {
      com.rti.ndds.config.Logger.get_instance().set_verbosity(LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
    } else if (LOGGER.isDebugEnabled()) {
      com.rti.ndds.config.Logger.get_instance().set_verbosity(LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_LOCAL);
    } else if (LOGGER.isWarnEnabled()) {
      com.rti.ndds.config.Logger.get_instance().set_verbosity(LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_WARNING);
    } else if (LOGGER.isErrorEnabled()) {
      com.rti.ndds.config.Logger.get_instance().set_verbosity(LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_ERROR);
    }

    // return logger
    return slf4jDdsLogger;
  }

  @Override
  public void write(
    final LogMessage logMessage
  ) {
    if (logMessage.level == NDDS_CONFIG_LOG_LEVEL_ERROR
      && LOGGER.isErrorEnabled()) {
      LOGGER.error(logMessage.text.replace('\n', ' '));
      return;
    }
    if (logMessage.level == NDDS_CONFIG_LOG_LEVEL_WARNING
      && LOGGER.isWarnEnabled()) {
      LOGGER.warn(logMessage.text.replace('\n', ' '));
      return;
    }
    if (logMessage.level == NDDS_CONFIG_LOG_LEVEL_STATUS_LOCAL
      && LOGGER.isTraceEnabled()) {
      LOGGER.trace(logMessage.text.replace('\n', ' '));
      return;
    }
    if (logMessage.level == NDDS_CONFIG_LOG_LEVEL_STATUS_REMOTE
      && LOGGER.isTraceEnabled()) {
      LOGGER.trace(logMessage.text.replace('\n', ' '));
      return;
    }
    if (logMessage.level == NDDS_CONFIG_LOG_LEVEL_DEBUG
      && LOGGER.isDebugEnabled()) {
      LOGGER.debug(logMessage.text.replace('\n', ' '));
    }
  }

  @Override
  public void close() {
    LOGGER.trace("Closing log device.");
  }
}
