# define parent
FROM eclipse-temurin:21-ubi9-minimal

# arguments
ARG BUILD_ARG_RTI_VERSION

# define maintainer
LABEL maintainer="Andreas Guther <andreas@guther.net>"
LABEL org.label-schema.name="rti-connext-dds-sdk"
LABEL org.label-schema.version="$BUILD_ARG_RTI_VERSION"

# add RTI Connext DDS runtime
COPY sdk/ /opt/rti/connext-dds/

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
