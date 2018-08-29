#!/usr/bin/env bash

# start build of docker file
docker build -t centos-java:7-1.8.0-openjdk .

# clean up files
rm -f *.rpm

# save docker image
docker save -o centos-java-7--1.8.0-openjdk.tar centos-java:7-1.8.0-openjdk

# gzip archive
gzip -f centos-java-7--1.8.0-openjdk.tar
