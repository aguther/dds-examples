# define parent
FROM rti-web-integration-service:6.1.0

# define maintainer
LABEL maintainer="Andreas Guther <andreas@guther.net>"
LABEL org.label-schema.name="rti-web-integration-service-shapes-demo"
LABEL org.label-schema.version="6.1.0"

# add shapes demo
ADD shapes-demo /app

# define work directory and entrypoint
ENTRYPOINT ["/usr/bin/stdbuf", "-oL", "-eL", "/opt/rti/connext-dds/bin/rtiwebintegrationservice", "-cfgName", "shapesDemoTutorial"]
