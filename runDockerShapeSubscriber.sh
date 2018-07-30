#!/usr/bin/env bash

# run docker container
docker run \
  --detach \
  --rm \
  --name shape-subscriber \
  dds-examples:latest \
  com.github.aguther.dds.examples.shape.ShapeSubscriber
