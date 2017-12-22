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

package com.github.aguther.dds.routing.adapter.dynamic;

import java.util.Objects;
import java.util.regex.Pattern;

public class Configuration {

  private Pattern allowTopicNameFilter;
  private Pattern denyTopicNameFilter;
  private Pattern allowPartitionNameFilter;
  private Pattern denyPartitionNameFilter;
  private String datareaderQos = "";
  private String datawriterQos = "";

  public Pattern getAllowTopicNameFilter() {
    return allowTopicNameFilter;
  }

  public void setAllowTopicNameFilter(
      Pattern allowTopicNameFilter
  ) {
    this.allowTopicNameFilter = allowTopicNameFilter;
  }

  public Pattern getDenyTopicNameFilter() {
    return denyTopicNameFilter;
  }

  public void setDenyTopicNameFilter(
      Pattern denyTopicNameFilter
  ) {
    this.denyTopicNameFilter = denyTopicNameFilter;
  }

  public Pattern getAllowPartitionNameFilter() {
    return allowPartitionNameFilter;
  }

  public void setAllowPartitionNameFilter(
      Pattern allowPartitionNameFilter
  ) {
    this.allowPartitionNameFilter = allowPartitionNameFilter;
  }

  public Pattern getDenyPartitionNameFilter() {
    return denyPartitionNameFilter;
  }

  public void setDenyPartitionNameFilter(
      Pattern denyPartitionNameFilter
  ) {
    this.denyPartitionNameFilter = denyPartitionNameFilter;
  }

  public String getDatareaderQos() {
    return datareaderQos;
  }

  public void setDatareaderQos(
      String datareaderQos
  ) {
    this.datareaderQos = datareaderQos;
  }

  public String getDatawriterQos() {
    return datawriterQos;
  }

  public void setDatawriterQos(
      String datawriterQos
  ) {
    this.datawriterQos = datawriterQos;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Configuration that = (Configuration) o;
    return Objects.equals(allowTopicNameFilter, that.allowTopicNameFilter) &&
        Objects.equals(denyTopicNameFilter, that.denyTopicNameFilter) &&
        Objects.equals(allowPartitionNameFilter, that.allowPartitionNameFilter) &&
        Objects.equals(denyPartitionNameFilter, that.denyPartitionNameFilter) &&
        Objects.equals(datareaderQos, that.datareaderQos) &&
        Objects.equals(datawriterQos, that.datawriterQos);
  }

  @Override
  public int hashCode() {

    return Objects
        .hash(allowTopicNameFilter, denyTopicNameFilter, allowPartitionNameFilter, denyPartitionNameFilter,
            datareaderQos,
            datawriterQos);
  }
}
