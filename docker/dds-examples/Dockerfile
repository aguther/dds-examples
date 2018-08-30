# define parent
FROM guther/centos-java:latest

# define maintainer
LABEL maintainer="andreas@guther.net" \
      application="rti-routing-service"

# add RTI Connext DDS runtime
ADD rti-connext-dds-53-runtime-pro-*.rpm /tmp

# install java
RUN yum -y install /tmp/*.rpm \
    && rm -f /tmp/*.rpm

# add application files
ADD dds-examples*all.jar /app/application.jar

# switch user to non-root
USER app

# define work directory and entrypoint
ENTRYPOINT ["/usr/bin/java", "-cp", "/app/application.jar"]
