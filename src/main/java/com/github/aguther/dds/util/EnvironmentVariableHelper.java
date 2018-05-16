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

package com.github.aguther.dds.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to resolve environment variable in a string.
 */
public class EnvironmentVariableHelper {

  private EnvironmentVariableHelper() {
  }

  private static final String ENVIRONMENT_VARIABLE_TEMPLATE_REGEX
      = "\\$\\{([A-Za-z0-9_]+)\\}";

  private static final Pattern ENVIRONMENT_VARIABLE_TEMPLATE_PATTERN
      = Pattern.compile(ENVIRONMENT_VARIABLE_TEMPLATE_REGEX);

  /**
   * Resolves environment variables given in the form '${ENVIRONMENT_VARIABLE}' within a given string.
   *
   * @param string string with environment variables to be resolved
   * @return string with environment variables resolved
   */
  public static String resolve(
      String string
  ) {
    // get matcher for property
    Matcher matcher = ENVIRONMENT_VARIABLE_TEMPLATE_PATTERN.matcher(string);

    // iterate over all matches
    while (matcher.find()) {
      // get environment variable if available
      String environmentVariableValue = System.getenv().get(matcher.group(1).toUpperCase());

      // when environment variable is not found -> exception
      if (environmentVariableValue == null) {
        throw new IllegalArgumentException("Error during resolution of environment variable");
      }

      // prepare environment variable for usage
      environmentVariableValue = environmentVariableValue.replace("\\", "\\\\");

      // replace all occurrences of the environment variable in the provided string
      string = Pattern.compile(Pattern.quote(matcher.group(0))).matcher(string).replaceAll(environmentVariableValue);
    }

    return string;
  }

}
