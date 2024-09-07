# define parent
FROM eclipse-temurin:21-alpine

# define maintainer
LABEL maintainer="Andreas Guther <andreas@guther.net>"
LABEL org.label-schema.name="rti-connext-dds-runtime"
#LABEL org.label-schema.version="6.1.2"

# add RTI Connext DDS runtime
ADD sdk/lib/java /opt/rti/connext-dds/lib/java/
ADD sdk/lib/x64Linux4gcc7.3.0/*.so* /opt/rti/connext-dds/lib/x64Linux4gcc7.3.0/
ADD sdk/resource/schema/definitions /opt/rti/connext-dds/resource/schema/definitions/
ADD sdk/resource/scripts /opt/rti/connext-dds/resource/scripts/

# add environment variables
ENV NDDSHOME=/opt/rti/connext-dds
ENV LD_LIBRARY_PATH=/opt/rti/connext-dds/lib/x64Linux4gcc7.3.0
ENV PATH=${PATH}:${NDDSHOME}/bin

# disable copy of workspace
RUN \
  sed -i 's/# copy_examples=false/copy_examples=false\ncopy_workspace=false/' ${NDDSHOME}/resource/scripts/rticommon_config.sh

# create workdir
RUN \
  mkdir /app

# set working directory
WORKDIR /app
