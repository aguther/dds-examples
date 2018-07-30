#!/usr/bin/env bash

# run docker container
#  --network host \
docker run \
  --detach \
  --rm \
  --name rti-routing-service \
  --publish 7400:7400/tcp \
  --volume $PWD/USER_QOS_PROFILES.xml:/app/USER_QOS_PROFILES.xml \
  --volume $PWD/configuration/routing-static-tcp.xml:/app/USER_ROUTING_SERVICE.xml \
  rti-routing-service:5.3.1 \
  -cfgName dds-examples-routing-static \
  -verbosity 3
