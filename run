#!/usr/bin/env bash

# prepare runtime environment
./gradlew -x test build copyRuntimeLibraries

# start application
exec /usr/bin/java -classpath build/libs/*:build/libs-runtime/* com.github.aguther.dds.examples.$1
