package com.github.aguther.dds.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EnvironmentVariableHelperTest {

  @Test
  public void resolveWithoutEnvironmentVariable() {
    assertEquals(
        "String without any environment variable.",
        EnvironmentVariableHelper.resolve("String without any environment variable.")
    );
  }

  @Test
  public void resolveWithEnvironmentVariableUser() {
    assertEquals(
        String.format("String with environment variable user that has the value '%s'.", System.getenv("USER")),
        EnvironmentVariableHelper.resolve("String with environment variable user that has the value '${USER}'.")
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void resolveWithNonExistingEnvironmentVariable() {
    EnvironmentVariableHelper.resolve("String with non-existing environment variable ${NON_EXISTING}.");
  }
}