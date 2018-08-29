# define parent
FROM centos-java:7-1.8.0-openjdk

# define maintainer
LABEL maintainer="andreas@guther.net" \
      application="rti-persistence-service" \
      version="5.3.1"

# add RTI Connext DDS runtime
ADD rti-connext-dds-53-persistence-service-*.rpm /tmp

# install java
RUN yum -y install /tmp/*.rpm \
    && rm -f /tmp/*.rpm

# switch user to non-root
USER app

# define work directory and entrypoint
ENTRYPOINT ["/usr/bin/stdbuf", "-oL", "-eL", "/usr/bin/rtipersistenceservice"]
