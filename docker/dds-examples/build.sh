#!/usr/bin/env bash

# goto top directory
pushd ./../../

# trigger build
./gradlew :shadowjar

# restore directory
popd

# copy needed files
cp -f ./../../build/libs/*all.jar .
cp -f ./../rpms/rti-connext-dds-53-runtime-pro-x64Linux3gcc4.8.2-5.3.1.0-3.x86_64.rpm .

# start build of docker file
docker build -t dds-examples:latest .

# clean up files
rm -f *.jar *.xml *.rpm

# save docker image
docker save -o dds-examples--latest.tar dds-examples:latest

# gzip archive
gzip -f dds-examples--latest.tar
