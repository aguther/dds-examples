#!/usr/bin/env bash

# determine script directory
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"

# start zookeeper
exec ${SCRIPT_DIR}/kafka/bin/zookeeper-server-start.sh ${SCRIPT_DIR}/kafka/config/zookeeper.properties |& logger --tag zookeeper &
