#!/usr/bin/env bash

# copy needed files
mkdir sdk
cp -r ${NDDSHOME}/* sdk/
rm -rf sdk/rti_license.dat

# set version
VERSION=6.1.2

# start build of docker file
docker build -t rti-routing-service:"${VERSION}" .

# clean up files
rm -rf sdk

# save docker image
docker save -o rti-routing-service--"${VERSION}".tar rti-routing-service:"${VERSION}"

# gzip archive
gzip -f rti-routing-service--"${VERSION}".tar
