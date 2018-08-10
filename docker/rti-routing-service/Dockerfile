# define parent
FROM centos:7

# define maintainer
LABEL maintainer="andreas@guther.net" \
      application="rti-routing-service" \
      version="5.3.1"

# add RTI Connext DDS runtime
ADD rti-connext-dds-53-runtime-pro-*.rpm /tmp
ADD rti-connext-dds-53-routing-service-*.rpm /tmp

# install java
RUN yum -y install /tmp/*.rpm \
    && rm -f /tmp/*.rpm \
    && groupadd -r app \
    && useradd -r -g app app \
    && mkdir -p /app \
    && chown -R app:app /app

# switch user to non-root
USER app

# define work directory and entrypoint
WORKDIR "/app"
ENTRYPOINT ["/usr/bin/stdbuf", "-oL", "-eL", "/usr/bin/rtiroutingservice"]
