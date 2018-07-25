# define parent
FROM centos:7

# define maintainer
LABEL maintainer="andreas@guther.net"

# add RTI Connext DDS runtime
ADD rti-connext-dds-53-runtime-pro-*.rpm /tmp

# install java
RUN yum -y install /tmp/*.rpm \
    && rm -f /tmp/*.rpm \
    && groupadd -r app \
    && useradd -r -g app app \
    && mkdir -p /app \
    && chown -R app:app /app

# add application files
ADD dds-examples*all.jar /app/application.jar
ADD USER_QOS_PROFILES.xml /app

# switch user to non-root
USER app

# define work directory and entrypoint
WORKDIR "/app"
ENTRYPOINT ["/usr/bin/java", "-cp", "/app/application.jar", "com.github.aguther.dds.examples.shape.ShapePublisher"]
