# define parent
FROM centos:7

# define maintainer
LABEL maintainer="Andreas Guther <andreas@guther.net>" \
      application="rti-admin-console" \
      version="5.3.1"

# add RTI Connext DDS Admin Console and License
ADD rti-connext-dds-53-admin-console-*.rpm /tmp
ADD rti-connext-dds-53-license-*.rpm /tmp

# install java
RUN yum -y install java-1.8.0-openjdk.x86_64 xeyes gtk2 libcanberra-gtk2 /tmp/*.rpm \
    && rm -rf /var/cache/yum \
    && rm -f /tmp/*.rpm \
    && groupadd -r app \
    && useradd -r -g app -d /app app \
    && mkdir -p /app \
    && chown -R app:app /app

# switch user to non-root -> this must be done in the final container
USER app

# define work directory
WORKDIR "/app"

# define work directory and entrypoint
ENTRYPOINT ["/usr/bin/env bash"]
