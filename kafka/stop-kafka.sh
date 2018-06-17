#!/usr/bin/env bash

# determine script directory
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"

# stop kafka
exec ${SCRIPT_DIR}/kafka/bin/kafka-server-stop.sh
