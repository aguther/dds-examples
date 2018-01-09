#!/usr/bin/env bash

# prepare runtime environment
./gradlew -x test build copyRuntimeLibraries

# set environment
export RTI_ROUTING_ADAPTER=$(find build/libs build/libs-runtime -name '*.jar' -printf '%p:' | sed 's/:$//')

# start application
exec /usr/bin/rtiroutingservice -cfgFile routing-adapter.xml -cfgName dds-examples-routing-adapter -verbosity 1
