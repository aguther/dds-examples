#!/usr/bin/env bash

# copy needed files
cp -f ./../rpms/rti-connext-dds-60-runtime-pro-x64Linux3gcc4.8.2-6.0.0.0-1.x86_64.rpm .
cp -f ./../rpms/rti-connext-dds-60-routing-service-6.0.0.0-1.x86_64.rpm .

# start build of docker file
docker build -t rti-routing-service:6.0.0 .

# clean up files
rm -f *.rpm

# save docker image
docker save -o rti-routing-service--6.0.0.tar rti-routing-service:6.0.0

# gzip archive
gzip -f rti-routing-service--6.0.0.tar
