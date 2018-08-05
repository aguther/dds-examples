#!/usr/bin/env bash

# copy needed files
cp -f ./../rpms/rti-connext-dds-53-persistence-service-5.3.1.0-2.x86_64.rpm .

# start build of docker file
docker build -t rti-persistence-service:5.3.1 .

# clean up files
rm -f *.rpm

# save docker image
docker save -o rti-persistence-service--5.3.1.tar rti-persistence-service:5.3.1

# gzip archive
gzip -f rti-persistence-service--5.3.1.tar
