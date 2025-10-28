FROM eclipse-temurin:17-jre
WORKDIR /opt/h2

# Install dependencies
RUN apt-get update && apt-get install -y netcat-openbsd curl && rm -rf /var/lib/apt/lists/*
RUN echo "nc got"

# Download H2 jar
RUN mkdir -p /opt/h2 \
    && curl -L https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar -o /opt/h2/h2.jar
RUN echo "h2 downloaded"

# Copy init files
COPY init.sql /opt/h2/init.sql
COPY init.sh /opt/h2/init.sh
RUN chmod +x /opt/h2/init.sh

EXPOSE 9092 81
ENTRYPOINT ["/opt/h2/init.sh"]
RUN echo "Done"
