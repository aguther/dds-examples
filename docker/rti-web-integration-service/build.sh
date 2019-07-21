#!/usr/bin/env bash

# copy needed files
cp -f ./../rpms/rti-connext-dds-60-web-integration-service-6.0.0.0-1.x86_64.rpm .

# start build of docker file
docker build -t rti-web-integration-service:6.0.0 .

# clean up files
rm -f *.rpm

# save docker image
docker save -o rti-web-integration-service--6.0.0.tar rti-web-integration-service:6.0.0

# gzip archive
gzip -f rti-web-integration-service--6.0.0.tar
