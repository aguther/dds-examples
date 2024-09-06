# DDS Examples
This is an repository that contains some simple use cases for the usage of RTI DDS.

## Copyright

The files contained in this repository are published under MIT license with one exception: ShapeType.idl

This file is part of the RTI Connext DDS SDK (see [www.rti.com](https://www.rti.com)) and is used by a simple application for demonstration.

The original license did not allow the publication and so I want to express a special thank you to RTI for
the allowance to publish the file under a new license as found in this repository.

## Structure
This example is using XML creation for the DDS. More information can be found on community.rti.com.

## Preparation

For the preparation of the dependencies, which are not available in maven central, the following needs to be executed:
```
mvn install:install-file -Dfile=$NDDSHOME/lib/java/nddsjava.jar -DgroupId=com.rti -DartifactId=nddsjava -Dversion=<version> -Dpackaging=jar
mvn install:install-file -Dfile=$NDDSHOME/lib/java/rticonnextmsg.jar -DgroupId=com.rti -DartifactId=rticonnextmsg -Dversion=<version> -Dpackaging=jar
mvn install:install-file -Dfile=$NDDSHOME/lib/java/rtiroutingservice.jar -DgroupId=com.rti -DartifactId=rtiroutingservice -Dversion=<version> -Dpackaging=jar
mvn install:install-file -Dfile=$NDDSHOME/lib/java/rtirsadapter.jar -DgroupId=com.rti -DartifactId=rtirsadapter -Dversion=<version> -Dpackaging=jar
```

## Building

The application can be built with:
`./gradlew build`

## Starting examples
The examples can be executed in the following ways:
`./run.sh <Example>`

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

## Docker containers

### Dependencies

The following RPMs are needed and must be placed into the directory `docker/rpms`:
* rti-connext-dds-53-cloud-discovery-service-5.3.1.0-2.x86_64.rpm
* rti-connext-dds-53-routing-service-5.3.1.0-2.x86_64.rpm
* rti-connext-dds-53-runtime-pro-x64Linux3gcc4.8.2-5.3.1.0-2.x86_64.rpm

In order to build these RPM files please refer to this repository: [github.com/aguther/rti-connext-dds-pro](https://www.github.com/aguther/rti-connext-dds-pro).

### Output

The build scripts included in the directories will copy the dependencies and build the images. The images are then
also exported to tar.gz files for easy reuse without a build environment.

They can be loaded in the following way:
`docker load -i shape-publisher.tar.gz`
