#!/usr/bin/env bash

# ensure directory for images exists
mkdir -p ./images

# build directory rti-cloud-discovery-service
#pushd rti-cloud-discovery-service || exit
#./build.sh
#popd || exit

# build directory rti-connext-dds-runtime
pushd rti-connext-dds-runtime || exit
./build.sh
popd || exit

# build directory rti-connext-dds-runtime-slim
pushd rti-connext-dds-runtime-slim || exit
./build.sh
popd || exit

# build directory rti-persistence-service
pushd rti-persistence-service || exit
./build.sh
popd || exit

# build directory rti-routing-service
pushd rti-routing-service || exit
./build.sh
popd || exit

# build directory rti-routing-service-slim
pushd rti-routing-service-slim || exit
./build.sh
popd || exit

# build directory rti-web-integration-service
pushd rti-web-integration-service || exit
./build.sh
popd || exit

# build directory rti-web-integration-service-shapes-demo
pushd rti-web-integration-service-shapes-demo || exit
./build.sh
popd || exit

# move images
find . -name "*.tar.gz" -exec mv {} ./images \;
