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

# set version
VERSION=6.1.2-$(git describe --always --dirty)

# start build of docker file
docker build -t dds-examples:"${VERSION}" .

# clean up files
rm -f *.jar *.xml *.rpm

# save docker image
docker save -o dds-examples--"${VERSION}".tar dds-examples:"${VERSION}"

# gzip archive
gzip -f dds-examples--"${VERSION}".tar
