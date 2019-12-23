# define parent
FROM guther/centos-java:latest

# define maintainer
LABEL maintainer="Andreas Guther <andreas@guther.net>" \
      application="rti-persistence-service" \
      version="6.0.1"

# add RTI Connext DDS runtime
ADD rti-connext-dds-60-persistence-service-*.rpm /tmp

# install java
RUN yum -y install /tmp/*.rpm \
    && rm -f /tmp/*.rpm

# switch user to non-root
USER app

# define work directory and entrypoint
ENTRYPOINT ["/usr/bin/stdbuf", "-oL", "-eL", "/usr/bin/rtipersistenceservice"]
