#!/usr/bin/env bash

# determine script directory
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"

# start zookeeper
${SCRIPT_DIR}/kafka/bin/zookeeper-server-start.sh ${SCRIPT_DIR}/kafka/config/zookeeper.properties |& logger &

# wait some time
sleep 4

# start kafka
${SCRIPT_DIR}/kafka/bin/kafka-server-start.sh ${SCRIPT_DIR}/kafka/config/server.properties |& logger &
