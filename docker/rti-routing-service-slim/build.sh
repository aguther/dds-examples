#!/usr/bin/env bash

# copy needed files
rm -rf .sdk
cp -r "${NDDSHOME}" ./sdk/
rm -rf ./sdk/rti_license.dat

# set version
VERSION=7.3.0

# start build of docker file
docker build --build-arg BUILD_ARG_RTI_VERSION=${VERSION} --tag rti-routing-service-slim:"${VERSION}" .

# clean up files
rm -rf sdk

# save docker image
docker save --output rti-routing-service-slim--"${VERSION}".tar rti-routing-service-slim:"${VERSION}"

# gzip archive
gzip -f rti-routing-service-slim--"${VERSION}".tar
