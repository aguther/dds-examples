#!/usr/bin/env bash

# copy shapes-demo
rm -rf shapes-demo
cp -r "${NDDSHOME}/resource/template/rti_workspace/examples/web_integration_service" ./shapes-demo/

# set version
VERSION=7.3.0

# start build of docker file
docker build --build-arg BUILD_ARG_RTI_VERSION=${VERSION} --tag rti-web-integration-service-shapes-demo:"${VERSION}" .

# clean up files
rm -rf shapes-demo

# save docker image
docker save --output rti-web-integration-service-shapes-demo--"${VERSION}".tar rti-web-integration-service-shapes-demo:"${VERSION}"

# gzip archive
gzip -f rti-web-integration-service-shapes-demo--"${VERSION}".tar
