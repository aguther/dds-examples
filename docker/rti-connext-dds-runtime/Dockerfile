# define parent
FROM eclipse-temurin:21-ubi9-minimal

# arguments
ARG BUILD_ARG_RTI_VERSION

# define maintainer
LABEL maintainer="Andreas Guther <andreas@guther.net>"
LABEL org.label-schema.name="rti-connext-dds-runtime"
LABEL org.label-schema.version="$BUILD_ARG_RTI_VERSION"

# add RTI Connext DDS runtime
COPY sdk/lib/java/*[^d].jar /opt/rti/connext-dds/lib/java/
COPY sdk/lib/x64Linux4gcc$BUILD_ARG_RTI_VERSION/*[^d].so* /opt/rti/connext-dds/lib/x64Linux4gcc$BUILD_ARG_RTI_VERSION/
COPY sdk/resource/schema/definitions /opt/rti/connext-dds/resource/schema/definitions/
COPY sdk/resource/scripts /opt/rti/connext-dds/resource/scripts/

# add environment variables
ENV NDDSHOME=/opt/rti/connext-dds
ENV LD_LIBRARY_PATH=/opt/rti/connext-dds/lib/x64Linux4gcc$BUILD_ARG_RTI_VERSION
ENV PATH=${PATH}:${NDDSHOME}/bin

# disable copy of workspace and create workdir
RUN \
  sed -i 's/# copy_examples=false/copy_examples=false\ncopy_workspace=false/' ${NDDSHOME}/resource/scripts/rticommon_config.sh \
  && mkdir /app

# set working directory
WORKDIR /app
