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
  * ShapePublisher
  * ShapeSubscriber
* Request/Reply
  * RequestReplyRequest
  * RequestReplyReply
* Mutable
  * MutableBuffer
  * MutablePublisher
  * MutableSubscriber
* Json
  * Json
* TBD: Read/Write to file
* TBD: Read/Write to file with backwards compatibility (mutable types)
* TBD: Stateless/Micro-Service

## Helper classes
The following helper classes are available:
* Logger that logs RTI messages via slf4j
* TypeAdapterFactory for Enum, Sequence for the usage with GSON library
* TBD: Listeners logging to slf4j and to multiple listeners (e.g. for Participant, Topic, DataReader, DataWriter)
* TBD: Helper for reading/taking data from DataReader
