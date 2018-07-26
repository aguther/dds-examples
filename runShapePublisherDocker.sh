#!/usr/bin/env bash

# run docker container
docker run \
  --detach \
  --rm \
  --name shape-publisher \
  --network host \
  shape-publisher:latest
