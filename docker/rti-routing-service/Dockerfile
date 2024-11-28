# define parent
FROM eclipse-temurin:21-ubi9-minimal

# arguments
ARG BUILD_ARG_RTI_VERSION

# define maintainer
LABEL maintainer="Andreas Guther <andreas@guther.net>"
LABEL org.label-schema.name="rti-routing-service"
LABEL org.label-schema.version="$BUILD_ARG_RTI_VERSION"

# add routing service
COPY sdk/bin/rtiroutingservice /opt/rti/connext-dds/bin/
COPY sdk/bin/rtirssh /opt/rti/connext-dds/bin/
COPY sdk/resource/app/bin/x64Linux3gcc4.8.2/rtiroutingserviceapp /opt/rti/connext-dds/resource/app/bin/x64Linux3gcc4.8.2/
COPY sdk/resource/app/bin/x64Linux3gcc4.8.2/rtirssh /opt/rti/connext-dds/resource/app/bin/x64Linux3gcc4.8.2/
COPY sdk/resource/app/lib/java /opt/rti/connext-dds/resource/app/lib/java/
COPY sdk/resource/app/lib/x64Linux3gcc4.8.2/*.so* /opt/rti/connext-dds/resource/app/lib/x64Linux3gcc4.8.2/
COPY sdk/resource/schema/rti_routing_service* /opt/rti/connext-dds/resource/schema/
COPY sdk/resource/schema/definitions /opt/rti/connext-dds/resource/schema/definitions/
COPY sdk/resource/scripts /opt/rti/connext-dds/resource/scripts/
COPY sdk/resource/xml/RTI_ROUTING_SERVICE.xml /opt/rti/connext-dds/resource/xml/

# add environment variables
ENV NDDSHOME=/opt/rti/connext-dds
ENV LD_LIBRARY_PATH=/opt/rti/connext-dds/lib/x64Linux3gcc5.4.0
ENV PATH=${PATH}:${NDDSHOME}/bin

# disable copy of workspace and create workdir
RUN \
  sed -i 's/# copy_examples=false/copy_examples=false\ncopy_workspace=false/' ${NDDSHOME}/resource/scripts/rticommon_config.sh \
  && mkdir /app

# set working directory
WORKDIR /app

# define work directory and entrypoint
ENTRYPOINT ["/usr/bin/stdbuf", "-oL", "-eL", "/opt/rti/connext-dds/bin/rtiroutingservice"]
