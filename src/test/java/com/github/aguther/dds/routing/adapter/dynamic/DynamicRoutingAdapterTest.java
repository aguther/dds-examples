package com.github.aguther.dds.routing.adapter.dynamic;

import static org.junit.Assert.assertNotNull;

import java.util.Properties;

public class DynamicRoutingAdapterTest {

  @org.junit.Test
  public void createConnection() {
  }

  @org.junit.Test
  public void getVersion() {
    DynamicRoutingAdapter dynamicRoutingAdapter = new DynamicRoutingAdapter(new Properties());
    assertNotNull(dynamicRoutingAdapter.getVersion());
  }
}
