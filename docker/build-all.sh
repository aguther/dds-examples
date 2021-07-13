#!/usr/bin/env bash

# ensure directory for images exists
mkdir -p ./images

# build directory rti-cloud-discovery-service
pushd rti-cloud-discovery-service
./build.sh
popd

# build directory rti-connext-dds-runtime
pushd rti-connext-dds-runtime
./build.sh
popd

# build directory rti-connext-dds-runtime-slim
pushd rti-connext-dds-runtime-slim
./build.sh
popd

# build directory rti-persistence-service
pushd rti-persistence-service
./build.sh
popd

# build directory rti-routing-service
pushd rti-routing-service
./build.sh
popd

# build directory rti-routing-service-slim
pushd rti-routing-service-slim
./build.sh
popd

# build directory rti-web-integration-service
pushd rti-web-integration-service
./build.sh
popd

# build directory dds-examples
pushd dds-examples
./build.sh
popd

# build directory rti-web-integration-service-shapes-demo
pushd rti-web-integration-service-shapes-demo
./build.sh
popd

# move images
find . -name "*.tar.gz" -exec mv {} ./images \;
