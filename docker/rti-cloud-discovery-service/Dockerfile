# define parent
FROM guther/centos-java:latest

# define maintainer
LABEL maintainer="Andreas Guther <andreas@guther.net>" \
      application="rti-cloud-discovery-service" \
      version="5.3.1"

# add RTI Connext DDS runtime
ADD rti-connext-dds-53-runtime-pro-*.rpm /tmp
ADD rti-connext-dds-53-cloud-discovery-service-*.rpm /tmp

# install java
RUN yum -y install /tmp/*.rpm \
    && rm -f /tmp/*.rpm

# switch user to non-root
USER app

# define work directory and entrypoint
ENTRYPOINT ["/usr/bin/stdbuf", "-oL", "-eL", "/usr/bin/rticlouddiscoveryservice"]
