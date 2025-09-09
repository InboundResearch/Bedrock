FROM tomcat:11.0-jdk21-temurin

# copy my war file
COPY ROOT.war /usr/local/tomcat/webapps/

# create a startup script to handle redirection of logging to a file we parse in the application
RUN echo '#!/bin/bash\n' \
    'exec /usr/local/tomcat/bin/catalina.sh run >> /usr/local/tomcat/logs/catalina.out 2>&1' > /usr/local/tomcat/start.sh \
    && chmod +x /usr/local/tomcat/start.sh

# tomcat runs on 8080
EXPOSE 8080

# Use the startup script as the container's entrypoint
ENTRYPOINT ["/usr/local/tomcat/start.sh"]
