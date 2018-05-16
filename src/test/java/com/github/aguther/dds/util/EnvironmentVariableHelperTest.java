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