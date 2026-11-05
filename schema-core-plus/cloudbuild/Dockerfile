FROM azul/zulu-openjdk:25.0.4.1-jre
WORKDIR /app
ARG PROVIDER_NAME
ENV PROVIDER_NAME core-plus

# Copy plugins
COPY tmp/os-oqm-rabbitmq-*.jar plugins/oqm-rabbitmq.jar
COPY tmp/os-obm-s3-*.jar plugins/obm-s3.jar
COPY tmp/os-osm-postgres-*.jar plugins/osm-postgres.jar

# Copy the jar to the production image from the builder stage.
COPY schema-${PROVIDER_NAME}/target/schema-${PROVIDER_NAME}-*-spring-boot.jar schema.jar
# Add a non-root user
RUN groupadd -g 10001 -r nonroot \
  && useradd -g 10001 -r -u 10001 nonroot
# Run as non-root user
USER 10001:10001
# Run the web service on container startup.
CMD java --add-opens java.base/java.lang=ALL-UNNAMED \
         --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
         -Djava.security.egd=file:/dev/./urandom \
         -Dserver.port=${PORT} \
         -Dlog4j.formatMsgNoLookups=true \
         -Dloader.path=plugins/ \
         -Dloader.main=org.opengroup.osdu.schema.CorePlusSchemaApplication \
         -jar /app/schema.jar
