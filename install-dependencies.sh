#!/usr/bin/env bash

export NDDS_VERSION=6.1.2

./mvnw install:install-file -Dfile=$NDDSHOME/lib/java/nddsjava.jar -DgroupId=com.rti -DartifactId=nddsjava -Dversion=$NDDS_VERSION -Dpackaging=jar
./mvnw install:install-file -Dfile=$NDDSHOME/lib/java/rticonnextmsg.jar -DgroupId=com.rti -DartifactId=rticonnextmsg -Dversion=$NDDS_VERSION -Dpackaging=jar
./mvnw install:install-file -Dfile=$NDDSHOME/lib/java/rtiroutingservice.jar -DgroupId=com.rti -DartifactId=rtiroutingservice -Dversion=$NDDS_VERSION -Dpackaging=jar
./mvnw install:install-file -Dfile=$NDDSHOME/lib/java/rtirsadapter.jar -DgroupId=com.rti -DartifactId=rtirsadapter -Dversion=$NDDS_VERSION -Dpackaging=jar
