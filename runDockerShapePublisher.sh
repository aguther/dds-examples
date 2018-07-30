#!/usr/bin/env bash

# run docker container
#  --network host \
docker run \
  --detach \
  --rm \
  --name shape-publisher \
  shape-publisher:latest
