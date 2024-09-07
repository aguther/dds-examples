# define parent
FROM debian:buster-slim

# define maintainer
LABEL maintainer="Andreas Guther <andreas@guther.net>"
LABEL org.label-schema.name="rti-cloud-discovery-service"
LABEL org.label-schema.version="6.1.2"

# add routing service
ADD sdk/bin/rticlouddiscoveryservice /opt/rti/connext-dds/bin/
ADD sdk/resource/app/bin/x64Linux2.6gcc4.4.5/rticlouddiscoveryserviceapp /opt/rti/connext-dds/resource/app/bin/x64Linux2.6gcc4.4.5/
ADD sdk/resource/app/lib/x64Linux2.6gcc4.4.5/*.so* /opt/rti/connext-dds/resource/app/lib/x64Linux2.6gcc4.4.5/
ADD sdk/resource/schema/rti_cloud_discovery_service* /opt/rti/connext-dds/resource/schema/
ADD sdk/resource/schema/definitions /opt/rti/connext-dds/resource/schema/definitions/
ADD sdk/resource/scripts /opt/rti/connext-dds/resource/scripts/
ADD sdk/resource/xml/RTI_CLOUD_DISCOVERY_SERVICE.xml /opt/rti/connext-dds/resource/xml/

# add environment variables
ENV NDDSHOME=/opt/rti/connext-dds
ENV LD_LIBRARY_PATH=/opt/rti/connext-dds/lib/x64Linux3gcc5.4.0
ENV PATH=${PATH}:${NDDSHOME}/bin

# disable copy of workspace
RUN \
  sed -i 's/# copy_examples=false/copy_examples=false\ncopy_workspace=false/' ${NDDSHOME}/resource/scripts/rticommon_config.sh

# create workdir
RUN \
  mkdir /app

# set working directory
WORKDIR /app

# define work directory and entrypoint
ENTRYPOINT ["/usr/bin/stdbuf", "-oL", "-eL", "/opt/rti/connext-dds/bin/rticlouddiscoveryservice"]
