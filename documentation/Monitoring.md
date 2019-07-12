# Monitoring for Prometheus

## Monitoring Library

### Entity structure

- Domain Participant
  - Topic
  - Publisher
    - Data Writer
      - Matched Subscriptions
  - Subscriber
    - Data Reader
      - Matched Publications

| Entity                       | Used topic                                               |
| ---------------------------- | -------------------------------------------------------- |
| Domain Participant           | DomainParticipantDescription                             |
|                              | DomainParticipantEntityStatistics                        |
| Topic                        | TopicDescription                                         |
|                              | TopicEntityStatistics                                    |
| Publisher                    | PublisherDescription                                     |
| Data Writer                  | DataWriterDescription                                    |
|                              | DataWriterEntityStatistics                               |
| Matched Subscriptions        | DataWriterEntityMatchedSubscriptionStatistics            |
| Matched Subscription Locator | DataWriterEntityMatchedSubscriptionWithLocatorStatistics |
| Subscriber                   | SubscriberDescription                                    |
| Data Reader                  | DataReaderDescription                                    |
|                              | DataReaderEntityStatistics                               |
| Matched Publications         | DataReaderEntityMatchedPublicationStatistics             |

## Routing Service


## Possible helpers
* https://github.com/j-easy/easy-batch
* https://github.com/google/guava
