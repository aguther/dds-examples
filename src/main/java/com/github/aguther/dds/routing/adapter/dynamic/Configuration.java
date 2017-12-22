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
