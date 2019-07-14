#!/usr/bin/env bash

# goto top directory
pushd ./../../

# trigger build
./gradlew :shadowjar

# restore directory
popd

# copy needed files
cp -f ./../../build/libs/*all.jar .
cp -f ./../rpms/rti-connext-dds-60-runtime-pro-x64Linux3gcc4.8.2-6.0.0.0-1.x86_64.rpm .

# set version
VERSION=6.0.0-$(git describe --always --dirty)

# start build of docker file
docker build -t dds-examples:"${VERSION}" .

# clean up files
rm -f *.jar *.xml *.rpm

# save docker image
docker save -o dds-examples--"${VERSION}".tar dds-examples:"${VERSION}"

# gzip archive
gzip -f dds-examples--"${VERSION}".tar
