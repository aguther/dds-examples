# define parent
FROM rti-web-integration-service:6.0.1

# define maintainer
LABEL maintainer="Andreas Guther <andreas@guther.net>" \
      application="rti-web-integration-service-shapes-demo" \
      version="6.0.1"

# add shapes demo
ADD shapes-demo /app

# switch user to non-root
USER app

# define work directory and entrypoint
ENTRYPOINT ["/usr/bin/stdbuf", "-oL", "-eL", "/usr/bin/rtiwebintegrationservice"]
