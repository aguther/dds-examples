#!/usr/bin/env bash

# copy needed files
mkdir sdk
cp -r ${NDDSHOME}/* sdk/

# set version
VERSION=6.1.0

# start build of docker file
docker build -t rti-cloud-discovery-service:"${VERSION}" .

# clean up files
rm -rf sdk

# save docker image
docker save -o rti-cloud-discovery-service--"${VERSION}".tar rti-cloud-discovery-service:"${VERSION}"

# gzip archive
gzip -f rti-cloud-discovery-service--"${VERSION}".tar
