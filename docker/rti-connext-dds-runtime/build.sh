#!/usr/bin/env bash

# copy needed files
rm -rf .sdk
cp -r "${NDDSHOME}" ./sdk/
rm -rf ./sdk/rti_license.dat

# set version
VERSION=7.3.0

# start build of docker file
docker build --build-arg BUILD_ARG_RTI_VERSION=${VERSION} --tag rti-connext-dds-runtime:"${VERSION}" .

# clean up files
rm -rf sdk

# save docker image
docker save --output rti-connext-dds-runtime--"${VERSION}".tar rti-connext-dds-runtime:"${VERSION}"

# gzip archive
gzip -f rti-connext-dds-runtime--"${VERSION}".tar
