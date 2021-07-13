# define parent
FROM rti-connext-dds-runtime:6.1.0

# define maintainer
LABEL maintainer="Andreas Guther <andreas@guther.net>"
LABEL org.label-schema.name="dds-examples"
#LABEL org.label-schema.version="6.1.0"

# add application files
ADD dds-examples*all.jar /app/application.jar
ADD USER_QOS_PROFILES.xml /app/USER_QOS_PROFILES.xml

# define work directory and entrypoint
ENTRYPOINT ["/usr/bin/java", "-cp", "/app/application.jar"]
