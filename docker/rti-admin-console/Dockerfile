# define parent
FROM ubuntu:18.10

# define maintainer
LABEL maintainer="Andreas Guther <andreas@guther.net>" \
      application="rti-admin-console" \
      version="5.3.1"

# create user
RUN groupadd -r app \
    && useradd -r -g app -d /app app \
    && mkdir -p /app \
    && chown -R app:app /app

# update
RUN apt-get update \
    && apt-get upgrade -y

# install alien
RUN apt-get update \
    && apt-get install -y software-properties-common \
    && add-apt-repository universe \
    && apt-get update \
    && apt-get install -y alien

# install broadway
RUN apt-get update \
    && apt-get install -y libgtk-3-bin

# add, install and patch RTI Connext DDS Admin Console and License
ADD start.sh /app
ADD rti-connext-dds-53-admin-console-*.rpm /tmp
ADD rti-connext-dds-53-license-*.rpm /tmp
RUN alien -i /tmp/*.rpm \
    && sed -i -e 's/SWT_GTK3=0/SWT_GTK3=1/g' /opt/rti/connext-dds/admin-console/bin/rtiadminconsole

# switch user to non-root -> this must be done in the final container
USER app

# define work directory
WORKDIR "/app"

# define work directory and entrypoint
ENTRYPOINT [ "/app/start.sh"]
