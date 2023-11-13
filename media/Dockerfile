FROM eclipse-temurin:17-jre-alpine
MAINTAINER Latipe
ARG JAR_FILE
COPY target/media-1.0.0-SNAPSHOT.jar media-1.0.0.jar
EXPOSE 7657
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "media-1.0.0.jar"]