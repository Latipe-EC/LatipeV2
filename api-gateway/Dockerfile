FROM eclipse-temurin:17-jre-alpine
MAINTAINER Latipe
ARG JAR_FILE
COPY target/api-1.0.0-SNAPSHOT.jar api-1.0.0.jar
EXPOSE 8181
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "api-1.0.0.jar"]