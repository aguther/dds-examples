# DDS Examples
This is an repository that contains some simple use cases for the usage of RTI DDS.

## Structure
This example is using XML creation for the DDS. More information can be found on community.rti.com.

## Building
The application can be built with:
`./gradlew build`

## Starting examples
The examples can be executed in the following ways:
`./run <Example>`

## Examples
* Shape
  * ShapePublisher `shape.ShapePublisher`
  * ShapeSubscriber `shape.ShapeSubscriber`
* Request/Reply
  * RequestReplyRequest `requestreply.Request`
  * RequestReplyReply `requestreply.Reply`
* Mutable
  * MutableBuffer `mutable.MutableBuffer`
  * MutablePublisher `mutable.MutablePublisher`
  * MutableSubscriber `mutable.MutableSubscriber`
* Json
  * Json `json.Json`
* Discovery
  * Discovery `discovery.Discovery`
* Routing
  * Static Routing `routing.StaticRouting`
  * Dynamic Routing `routing.DynamicRouting`
  * Routing with Adapter `use ./runRoutingAdapter`

## Routing Service examples
The examples can be used with the ShapeDemo. Start a ShapeDemo on domain 0 and a ShapeDemo on domain 1.

## Helper classes
The following helper classes are available.

### Logging
Logger that logs RTI messages via slf4j

### GSON library
TypeAdapterFactory for Enum, Sequence for the usage with GSON library

### Routing Service
Command helper that helps in sending remote commands to a routing service.

### Samples
Convert samples from/to byte buffer (in CDR format) and from/to DynamicData.

### Miscellaneous
* Get participant data from publication/subscription data
* Switch the auto-enable behaviour for created entities
* Check if a domain participant is enabled
* Get a duration from a Java time value and time unit
