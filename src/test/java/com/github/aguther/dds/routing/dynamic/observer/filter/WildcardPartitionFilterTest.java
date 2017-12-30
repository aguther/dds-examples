package com.github.aguther.dds.routing.dynamic.observer.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WildcardPartitionFilterTest {

  private WildcardPartitionFilter filter;

  @Before
  public void setUp() {
    filter = new WildcardPartitionFilter();
  }

  @After
  public void tearDown() {
    filter = null;
  }

  @Test
  public void testIgnorePublicationFalse() {
    assertFalse(filter.ignorePublication(null, null, null));
  }

  @Test
  public void testIgnoreSubscriptionFalse() {
    assertFalse(filter.ignoreSubscription(null, null, null));
  }

  @Test
  public void testIgnorePartition() {
    assertFalse(filter.ignorePartition("A", ""));
    assertFalse(filter.ignorePartition("A", "A"));
    assertTrue(filter.ignorePartition("A", "*"));
    assertTrue(filter.ignorePartition("A", "A*"));
  }
}