#!/usr/bin/env bash

# start broadway
broadwayd &

# setup environment
export GDK_BACKEND=broadway
export UBUNTU_MENUPROXY=
export LIBOVERLAY_SCROLLBAR=0

# start admin console
/usr/bin/rtiadminconsole

# keep container running
exec sleep infinity
