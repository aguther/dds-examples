#!/usr/bin/env bash

# delete current file
rm -rf kafka

# create target directory
mkdir kafka

# extract
tar xzf kafka.tgz -C kafka --strip-components 1
