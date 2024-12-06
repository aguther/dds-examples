#!/usr/bin/env bash

# start application
exec ./mvnw exec:java -D dds.example="$1"
