#!/usr/bin/env bash

# determine script directory
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"

# test if zookeeper is running
if ! nc -z localhost 2181; then
  echo "ZooKeeper is not running!"
  exit 1
fi

# start kafka
exec ${SCRIPT_DIR}/kafka/bin/kafka-server-start.sh ${SCRIPT_DIR}/kafka/config/server.properties |& logger --tag kafka &
