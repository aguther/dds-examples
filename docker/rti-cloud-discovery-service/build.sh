#!/usr/bin/env bash

# copy needed files
cp -f ./../rpms/rti-connext-dds-53-runtime-pro-x64Linux3gcc4.8.2-5.3.1.0-2.x86_64.rpm .
cp -f ./../rpms/rti-connext-dds-53-cloud-discovery-service-5.3.1.0-2.x86_64.rpm .

# start build of docker file
docker build -t rti-cloud-discovery-service:5.3.1 .

# clean up files
rm -f *.rpm

# save docker image
docker save -o rti-cloud-discovery-service--5.3.1.tar rti-cloud-discovery-service:5.3.1

# gzip archive
gzip -f rti-cloud-discovery-service--5.3.1.tar
