#!/usr/bin/env bash

# copy needed files
#cp -f ./../rpms/rti-connext-dds-60-runtime-pro-x64Linux3gcc4.8.2-6.0.1.0-1.x86_64.rpm .
cp -f ./../rpms/rti-connext-dds-60-cloud-discovery-service-6.0.1.0-1.x86_64.rpm .

# start build of docker file
docker build -t rti-cloud-discovery-service:6.0.1 .

# clean up files
rm -f *.rpm

# save docker image
docker save -o rti-cloud-discovery-service--6.0.1.tar rti-cloud-discovery-service:6.0.1

# gzip archive
gzip -f rti-cloud-discovery-service--6.0.1.tar
