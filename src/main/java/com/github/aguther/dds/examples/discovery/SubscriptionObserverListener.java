package com.github.aguther.dds.examples.discovery;

import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;

public interface SubscriptionObserverListener {

  public void subscriptionDiscovered(
      InstanceHandle_t instanceHandle,
      SubscriptionBuiltinTopicData data
  );

  public void subscriptionLost(
      InstanceHandle_t instanceHandle
  );
}
