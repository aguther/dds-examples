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

package com.github.aguther.dds.routing.dynamic;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * This class represents one configuration set for dynamic routing.
 */
public class Configuration {

  private Pattern allowTopicNameFilter;
  private Pattern denyTopicNameFilter;
  private Pattern allowPartitionNameFilter;
  private Pattern denyPartitionNameFilter;
  private String topicRouteQos = "";
  private String inputQos = "";
  private String outputQos = "";
  private String partitionTransformationRegex = "";
  private String partitionTransformationReplacement = "";

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

  public String getQosTopicRoute() {
    return topicRouteQos;
  }

  public void setTopicRouteQosQos(
    String topicRouteQos
  ) {
    this.topicRouteQos = topicRouteQos;
  }

  public String getQosInput() {
    return inputQos;
  }

  public void setQosInput(
    String inputQos
  ) {
    this.inputQos = inputQos;
  }

  public String getQosOutput() {
    return outputQos;
  }

  public void setQosOutput(
    String outputQos
  ) {
    this.outputQos = outputQos;
  }

  public String getPartitionTransformationRegex() {
    return partitionTransformationRegex;
  }

  public void setPartitionTransformationRegex(
    String partitionTransformationRegex
  ) {
    this.partitionTransformationRegex = partitionTransformationRegex;
  }

  public String getPartitionTransformationReplacement() {
    return partitionTransformationReplacement;
  }

  public void setPartitionTransformationReplacement(
    String partitionTransformationReplacement
  ) {
    this.partitionTransformationReplacement = partitionTransformationReplacement;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Configuration)) {
      return false;
    }
    Configuration that = (Configuration) o;
    return Objects.equals(allowTopicNameFilter, that.allowTopicNameFilter) &&
      Objects.equals(denyTopicNameFilter, that.denyTopicNameFilter) &&
      Objects.equals(allowPartitionNameFilter, that.allowPartitionNameFilter) &&
      Objects.equals(denyPartitionNameFilter, that.denyPartitionNameFilter) &&
      Objects.equals(topicRouteQos, that.topicRouteQos) &&
      Objects.equals(inputQos, that.inputQos) &&
      Objects.equals(outputQos, that.outputQos) &&
      Objects.equals(partitionTransformationRegex, that.partitionTransformationRegex) &&
      Objects.equals(partitionTransformationReplacement, that.partitionTransformationReplacement);
  }

  @Override
  public int hashCode() {
    return Objects
      .hash(allowTopicNameFilter, denyTopicNameFilter, allowPartitionNameFilter, denyPartitionNameFilter, topicRouteQos,
        inputQos, outputQos, partitionTransformationRegex, partitionTransformationReplacement);
  }
}
