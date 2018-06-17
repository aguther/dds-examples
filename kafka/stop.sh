#!/usr/bin/env bash

# determine script directory
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"

# stop kafka
${SCRIPT_DIR}/stop-kafka.sh

# stop zookeeper
${SCRIPT_DIR}/stop-zookeeper.sh
