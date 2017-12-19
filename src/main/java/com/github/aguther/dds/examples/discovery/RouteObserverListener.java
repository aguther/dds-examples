package com.github.aguther.dds.examples.discovery;

public interface RouteObserverListener {

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
