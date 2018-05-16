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
