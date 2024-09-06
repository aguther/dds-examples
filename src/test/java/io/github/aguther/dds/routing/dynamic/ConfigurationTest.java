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

package io.github.aguther.dds.routing.dynamic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;
import org.junit.Test;

public class ConfigurationTest {

  @Test(timeout = 4000)
  public void test00() throws Throwable {
    Configuration configuration0 = new Configuration();
    configuration0.setTopicRouteQosQos((String) null);
    String string0 = configuration0.getQosTopicRoute();
    assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test01() throws Throwable {
    Configuration configuration0 = new Configuration();
    configuration0.setTopicRouteQosQos("47;Oan;b");
    String string0 = configuration0.getQosTopicRoute();
    assertEquals("47;Oan;b", string0);
  }

  @Test(timeout = 4000)
  public void test02() throws Throwable {
    Configuration configuration0 = new Configuration();
    configuration0.setQosOutput("wRpD{");
    String string0 = configuration0.getQosOutput();
    assertEquals("wRpD{", string0);
  }

  @Test(timeout = 4000)
  public void test03() throws Throwable {
    Configuration configuration0 = new Configuration();
    configuration0.setQosInput((String) null);
    String string0 = configuration0.getQosInput();
    assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test04() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = Pattern.compile("7e:P");
    configuration0.setDenyTopicNameFilter(pattern0);
    Pattern pattern1 = configuration0.getDenyTopicNameFilter();
    assertSame(pattern1, pattern0);
  }

  @Test(timeout = 4000)
  public void test05() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = Pattern.compile("-sL&Cz>K>]I2T", 6);
    configuration0.setDenyTopicNameFilter(pattern0);
    Pattern pattern1 = configuration0.getDenyTopicNameFilter();
    assertEquals(6, pattern1.flags());
  }

  @Test(timeout = 4000)
  public void test06() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = Pattern.compile("|a2|\"eQ8AH ", (-1700));
    configuration0.setDenyTopicNameFilter(pattern0);
    Pattern pattern1 = configuration0.getDenyTopicNameFilter();
    assertSame(pattern1, pattern0);
  }

  @Test(timeout = 4000)
  public void test07() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = Pattern.compile("");
    configuration0.setDenyPartitionNameFilter(pattern0);
    Pattern pattern1 = configuration0.getDenyPartitionNameFilter();
    assertEquals("", pattern1.pattern());
  }

  @Test(timeout = 4000)
  public void test08() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = Pattern.compile(",+!V#'Sp", 770);
    configuration0.setDenyPartitionNameFilter(pattern0);
    Pattern pattern1 = configuration0.getDenyPartitionNameFilter();
    assertEquals(",+!V#'Sp", pattern1.toString());
  }

  @Test(timeout = 4000)
  public void test09() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = Pattern.compile("|a2|\"eQ8AH ", (-1700));
    configuration0.setDenyPartitionNameFilter(pattern0);
    Pattern pattern1 = configuration0.getDenyPartitionNameFilter();
    assertEquals("|a2|\"eQ8AH ", pattern1.toString());
  }

  @Test(timeout = 4000)
  public void test10() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = Pattern.compile("");
    configuration0.setAllowTopicNameFilter(pattern0);
    Pattern pattern1 = configuration0.getAllowTopicNameFilter();
    assertEquals("", pattern1.toString());
  }

  @Test(timeout = 4000)
  public void test11() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = Pattern.compile("%E", 72);
    configuration0.setAllowTopicNameFilter(pattern0);
    Pattern pattern1 = configuration0.getAllowTopicNameFilter();
    assertEquals("%E", pattern1.toString());
  }

  @Test(timeout = 4000)
  public void test12() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = Pattern.compile(",+!V#'Sp", 770);
    configuration0.setAllowPartitionNameFilter(pattern0);
    Pattern pattern1 = configuration0.getAllowPartitionNameFilter();
    assertEquals(834, pattern1.flags());
  }

  @Test(timeout = 4000)
  public void test13() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = Pattern.compile("|a2|\"eQ8AH ", (-1700));
    configuration0.setAllowPartitionNameFilter(pattern0);
    Pattern pattern1 = configuration0.getAllowPartitionNameFilter();
    assertEquals("|a2|\"eQ8AH ", pattern1.toString());
  }

  @Test(timeout = 4000)
  public void test14() throws Throwable {
    Configuration configuration0 = new Configuration();
    configuration0.setQosOutput(">7");
    Configuration configuration1 = new Configuration();
    boolean boolean0 = configuration0.equals(configuration1);
    assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test15() throws Throwable {
    Configuration configuration0 = new Configuration();
    Configuration configuration1 = new Configuration();
    assertTrue(configuration1.equals((Object) configuration0));

    configuration1.setQosInput("8wLg&%&go");
    boolean boolean0 = configuration0.equals(configuration1);
    assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test16() throws Throwable {
    Configuration configuration0 = new Configuration();
    Configuration configuration1 = new Configuration();
    assertTrue(configuration1.equals((Object) configuration0));

    Pattern pattern0 = Pattern.compile("", 2504);
    configuration1.setDenyPartitionNameFilter(pattern0);
    boolean boolean0 = configuration0.equals(configuration1);
    assertFalse(configuration1.equals((Object) configuration0));
    assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test17() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = Pattern.compile("qw<J7,gK");
    configuration0.setAllowPartitionNameFilter(pattern0);
    Configuration configuration1 = new Configuration();
    boolean boolean0 = configuration0.equals(configuration1);
    assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test18() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = Pattern.compile("", (-1700));
    configuration0.setAllowTopicNameFilter(pattern0);
    Configuration configuration1 = new Configuration();
    boolean boolean0 = configuration0.equals(configuration1);
    assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test19() throws Throwable {
    Configuration configuration0 = new Configuration();
    Configuration configuration1 = new Configuration();
    boolean boolean0 = configuration0.equals(configuration1);
    assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test20() throws Throwable {
    Configuration configuration0 = new Configuration();
    boolean boolean0 = configuration0.equals((Object) null);
    assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test21() throws Throwable {
    Configuration configuration0 = new Configuration();
    boolean boolean0 = configuration0.equals(configuration0);
    assertTrue(boolean0);
  }

  @Test(timeout = 4000)
  public void test22() throws Throwable {
    Configuration configuration0 = new Configuration();
    Object object0 = new Object();
    boolean boolean0 = configuration0.equals(object0);
    assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test23() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = configuration0.getAllowTopicNameFilter();
    assertNull(pattern0);
  }

  @Test(timeout = 4000)
  public void test24() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = Pattern.compile("|a2|\"eQ8AH ", (-1700));
    configuration0.setAllowTopicNameFilter(pattern0);
    Pattern pattern1 = configuration0.getAllowTopicNameFilter();
    assertSame(pattern1, pattern0);
  }

  @Test(timeout = 4000)
  public void test25() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = Pattern.compile("7e:P");
    configuration0.setDenyTopicNameFilter(pattern0);
    Configuration configuration1 = new Configuration();
    boolean boolean0 = configuration0.equals(configuration1);
    assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test26() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = Pattern.compile("7e:P");
    configuration0.setAllowPartitionNameFilter(pattern0);
    Pattern pattern1 = configuration0.getAllowPartitionNameFilter();
    assertEquals(0, pattern1.flags());
  }

  @Test(timeout = 4000)
  public void test27() throws Throwable {
    Configuration configuration0 = new Configuration();
    configuration0.hashCode();
  }

  @Test(timeout = 4000)
  public void test28() throws Throwable {
    Configuration configuration0 = new Configuration();
    String string0 = configuration0.getQosInput();
    assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test29() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = configuration0.getDenyPartitionNameFilter();
    assertNull(pattern0);
  }

  @Test(timeout = 4000)
  public void test30() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = configuration0.getAllowPartitionNameFilter();
    assertNull(pattern0);
  }

  @Test(timeout = 4000)
  public void test31() throws Throwable {
    Configuration configuration0 = new Configuration();
    configuration0.setQosOutput((String) null);
    String string0 = configuration0.getQosOutput();
    assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test32() throws Throwable {
    Configuration configuration0 = new Configuration();
    String string0 = configuration0.getQosOutput();
    assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test33() throws Throwable {
    Configuration configuration0 = new Configuration();
    configuration0.setTopicRouteQosQos((String) null);
    Configuration configuration1 = new Configuration();
    boolean boolean0 = configuration0.equals(configuration1);
    assertFalse(boolean0);
  }

  @Test(timeout = 4000)
  public void test34() throws Throwable {
    Configuration configuration0 = new Configuration();
    Pattern pattern0 = configuration0.getDenyTopicNameFilter();
    assertNull(pattern0);
  }

  @Test(timeout = 4000)
  public void test35() throws Throwable {
    Configuration configuration0 = new Configuration();
    configuration0.setQosInput("7e:P");
    String string0 = configuration0.getQosInput();
    assertEquals("7e:P", string0);
  }

  @Test(timeout = 4000)
  public void test36() throws Throwable {
    Configuration configuration0 = new Configuration();
    String string0 = configuration0.getQosTopicRoute();
    assertEquals("", string0);
  }
}
