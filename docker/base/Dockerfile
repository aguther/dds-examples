# define parent
FROM centos:7

# define maintainer
LABEL maintainer="andreas@guther.net" \
      java="java-1.8.0-openjdk"

# add non-root user and install java
RUN yum -y install java-1.8.0-openjdk.x86_64 \
    && rm -f /tmp/*.rpm \
    && groupadd -r app \
    && useradd -r -g app app \
    && mkdir -p /app \
    && chown -R app:app /app

# switch user to non-root -> this must be done in the final container
# USER app

# define work directory
WORKDIR "/app"
