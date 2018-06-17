#!/usr/bin/env bash

# determine script directory
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"

# stop kafka
${SCRIPT_DIR}/kafka/bin/kafka-server-stop.sh

# stop zookeeper
${SCRIPT_DIR}/kafka/bin/zookeeper-server-stop.sh
