FROM eclipse-temurin:17-jre-alpine
MAINTAINER Latipe
ARG JAR_FILE
COPY target/store-1.0.0-SNAPSHOT.jar store-1.0.0.jar
EXPOSE 8645
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "store-1.0.0.jar"]