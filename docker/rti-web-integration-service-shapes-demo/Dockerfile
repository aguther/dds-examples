# arguments for image
ARG BUILD_ARG_RTI_VERSION=latest

# define parent
FROM rti-web-integration-service:$BUILD_ARG_RTI_VERSION

# arguments
ARG BUILD_ARG_RTI_VERSION

# define maintainer
LABEL maintainer="Andreas Guther <andreas@guther.net>"
LABEL org.label-schema.name="rti-web-integration-service-shapes-demo"
LABEL org.label-schema.version="$BUILD_ARG_RTI_VERSION"

# add shapes demo
COPY shapes-demo /app

# define work directory and entrypoint
ENTRYPOINT ["/usr/bin/stdbuf", "-oL", "-eL", "/opt/rti/connext-dds/bin/rtiwebintegrationservice", "-cfgName", "shapesDemoTutorial"]
