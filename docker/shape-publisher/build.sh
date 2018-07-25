#!/usr/bin/env bash

# goto top directory
pushd ./../../

# trigger build
./gradlew :shadowjar

# restore directory
popd

# copy needed files
cp -f ./../../build/libs/*all.jar .
cp -f ./../../USER_QOS_PROFILES.xml .
cp -f ./../rpms/rti-connext-dds-53-runtime-pro-x64Linux3gcc4.8.2-5.3.1.0-2.x86_64.rpm .

# start build of docker file
docker build -t shape-publisher:latest .

# clean up files
rm -f *.jar *.xml *.rpm

# save docker image
docker save -o shape-publisher.tar shape-publisher:latest

# gzip archive
gzip -f shape-publisher.tar
