#!/usr/bin/env bash

# determine script directory
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"

# start zookeeper
${SCRIPT_DIR}/start-zookeeper.sh

# wait until zookeeper is running
while ! nc -z localhost 2181; do
  sleep 0.25
done

# start zookeeper
${SCRIPT_DIR}/start-kafka.sh
