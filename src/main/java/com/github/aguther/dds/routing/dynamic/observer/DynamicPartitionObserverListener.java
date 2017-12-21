package com.github.aguther.dds.routing.dynamic.observer;

public interface DynamicPartitionObserverListener {

  public void createSession(
      Session session
  );

  public void deleteSession(
      Session session
  );

  public void createTopicRoute(
      Session session,
      TopicRoute topicRoute
  );

  public void deleteTopicRoute(
      Session session,
      TopicRoute topicRoute
  );
}
