#!/usr/bin/env bash

# copy shapes-demo
cp -r /opt/rti/connext-dds/sdk/resource/template/rti_workspace/examples/web_integration_service shapes-demo

# set version
VERSION=6.0.0-$(git describe --always --dirty)

# start build of docker file
docker build -t rti-web-integration-service-shapes-demo:"${VERSION}" .

# clean up
rm -rf shapes-demo

# save docker image
docker save -o rti-web-integration-service-shapes-demo--"${VERSION}".tar rti-web-integration-service-shapes-demo:"${VERSION}"

# gzip archive
gzip -f rti-web-integration-service-shapes-demo--"${VERSION}".tar
