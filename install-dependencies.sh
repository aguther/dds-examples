#!/usr/bin/env bash

# prompt for version
read -p "Enter the NDDS version: " NDDS_VERSION

# define version
export NDDS_VERSION

# copy needed idl from SDK
cp $NDDSHOME/resource/idl/dds_rtf2_dcps.idl ./src/main/idl/
cp $NDDSHOME/resource/idl/monitoring.idl ./src/main/idl/
cp $NDDSHOME/resource/idl/RecordingServiceMonitoring.idl ./src/main/idl/
cp $NDDSHOME/resource/idl/RoutingServiceMonitoring.idl ./src/main/idl/
cp $NDDSHOME/resource/idl/ServiceAdmin.idl ./src/main/idl/
cp $NDDSHOME/resource/idl/ServiceCommon.idl ./src/main/idl/
cp $NDDSHOME/resource/idl/ServiceMonitoring.idl ./src/main/idl/

# install needed dependencies into local maven
./mvnw install:install-file -Dfile=$NDDSHOME/lib/java/nddsjava.jar -DgroupId=com.rti -DartifactId=nddsjava -Dversion=$NDDS_VERSION -Dpackaging=jar
./mvnw install:install-file -Dfile=$NDDSHOME/lib/java/rticonnextmsg.jar -DgroupId=com.rti -DartifactId=rticonnextmsg -Dversion=$NDDS_VERSION -Dpackaging=jar
./mvnw install:install-file -Dfile=$NDDSHOME/lib/java/rtiroutingservice.jar -DgroupId=com.rti -DartifactId=rtiroutingservice -Dversion=$NDDS_VERSION -Dpackaging=jar
./mvnw install:install-file -Dfile=$NDDSHOME/lib/java/rtirsadapter.jar -DgroupId=com.rti -DartifactId=rtirsadapter -Dversion=$NDDS_VERSION -Dpackaging=jar
