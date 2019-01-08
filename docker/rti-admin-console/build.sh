#!/usr/bin/env bash

# remove packed archive
rm -f rti-admin-console--5.3.1.tar*

# copy needed files
cp -f ./../rpms/rti-connext-dds-53-admin-console-5.3.1.14-1.x86_64.rpm .
cp -f ./../rpms/rti-connext-dds-53-license-5.3.1.14-1.x86_64.rpm .

# start build of docker file
docker build -t rti-admin-console:5.3.1 .

# clean up files
rm -f *.rpm

# save docker image
docker save -o rti-admin-console--5.3.1.tar rti-admin-console:5.3.1

# gzip archive
gzip -f rti-admin-console--5.3.1.tar
