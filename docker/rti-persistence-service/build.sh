#!/usr/bin/env bash

# copy needed files
rm -rf .sdk
cp -r "${NDDSHOME}" ./sdk/
rm -rf ./sdk/rti_license.dat

# set version
VERSION=7.3.0

# start build of docker file
docker build --build-arg BUILD_ARG_RTI_VERSION=${VERSION} --tag rti-persistence-service:"${VERSION}" .

# clean up files
rm -rf sdk

# save docker image
docker save --output rti-persistence-service--"${VERSION}".tar rti-persistence-service:"${VERSION}"

# gzip archive
gzip -f rti-persistence-service--"${VERSION}".tar
