#!/usr/bin/env bash

# determine script directory
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"

# stop zookeeper
exec ${SCRIPT_DIR}/kafka/bin/zookeeper-server-stop.sh
